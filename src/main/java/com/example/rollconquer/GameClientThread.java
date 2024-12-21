package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Player player; // Oggetto Player per ogni client
    private static final Random random = new Random();
    public static final List<GameClientThread> playersList = Collections.synchronizedList(new ArrayList<>());
    private static final Cell[] board = new Cell[100]; // Simuliamo la board di gioco
    private final ServerGame server;
    private static final List<GameClientThread> finalPlayers = Collections.synchronizedList(new ArrayList<>()); // Lista giocatori alla cella finale

    public int turni = 0;  // Contatore turni

    // Costruttore
    public GameClientThread(ServerGame server, Socket socket) {
        this.socket = socket;
        this.server = server;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Errore inizializzazione GameClientThread", e);
        }

        // Inizializzazione del tabellone (per semplicità)
        initializeBoard();
    }

    @Override
    public void run() {
        try {
            // Ricevi il nome del giocatore all'inizio
            String playerName = in.readLine();
            this.player = new Player(playerName); // Associa il giocatore al thread
            System.out.println("Nuovo giocatore connesso: " + player.getName());
            broadcast(player.getName() + " si è unito alla partita.");

            out.println("Benvenuto, " + player.getName() + "! Scrivi 'lancia' per lanciare i dadi.");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("lancia") && player.historyThrow.size() < this.server.loopCount) {
                    player.historyThrow.add(handleDiceRoll());
                    raiseCountLoop();
                } else if (message.equalsIgnoreCase("info")) {
                    player.showInfo();
                } else {
                    out.println("comando non valido, usare lancia o info.");
                    //System.out.println(player.getName() + ": " + message);
                    //broadcast(player.getName() + ": " + message);
                }

            }
        } catch (IOException e) {
            System.out.println(player.getName() + " si è disconnesso.");
            playersList.remove(this);
        } finally {
            synchronized (playersList) {
                playersList.remove(this);
            }
            broadcast(player.getName() + " ha lasciato la partita.");
        }
    }

    private void raiseCountLoop() {
        boolean flag = true;
        for (GameClientThread game : playersList) {
            if (game.player.historyThrow.size() < this.server.loopCount) {
                flag = false;
                break;
            }
        }
        if (flag) {
            this.server.loopCount++;
        }
    }

    // Metodo per gestire il lancio dei dadi
    // Metodo per gestire il lancio dei dadi
    private int handleDiceRoll() {
        turni++;
        int diceRoll = rollDice(6); // Primo dado a 6 facce
        int diceRoll2 = diceRoll;
        out.println("Hai la prima volta lanciato un " + diceRoll2);
        diceRoll += rollDice(6); // Secondo dado a 6 facce

        int movement = player.calculateMovement(diceRoll);
        player.move(movement, board); // Muove il giocatore sulla board
        out.println("Hai la seconda volta lanciato un " + (diceRoll - diceRoll2));
        //   out.println("Nuova posizione: " + player.getPosition());

        // Invio delle informazioni al client
        /*out.println("Hai lanciato un " + diceRoll + ".");
        out.println("Nuova posizione: " + player.getPosition());*/

        // Mostra informazioni aggiornate
        out.println(player.showInfo() + player.getLastZone());
        out.println("Turni " + turni);

        broadcast(player.getName() + " ha lanciato un " + diceRoll + " e ora è in posizione " + player.getPosition());

        // Controllo se il giocatore ha raggiunto la cella finale
        if (player.getPosition() >= board.length - 1) {
            synchronized (finalPlayers) {
                finalPlayers.add(this);
                broadcast(player.getName() + " ha raggiunto la cella finale!");

                // Se tutti hanno completato il turno e ci sono più finalisti, avvia lo spareggio
                if (allPlayersFinishedTurn() && finalPlayers.size() > 1) {
                    performTieBreaker();
                } else if (finalPlayers.size() == 1) { // Solo un giocatore ha finito
                    broadcast(player.getName() + " ha vinto la partita!");
                    endGame();
                }
            }
        }
        return diceRoll;
    }

    // Metodo per verificare se tutti i giocatori hanno completato il turno
    private boolean allPlayersFinishedTurn() {
        for (GameClientThread game : playersList) {
            if (game.player.historyThrow.size() < this.server.loopCount) {
                return false;
            }
        }
        return true;
    }

    // Metodo per spareggio tra i finalisti
    private void performTieBreaker() {
        broadcast("Spareggio tra i giocatori finalisti!");

        int highestRoll = 0;
        GameClientThread winner = null;

        for (GameClientThread playerThread : finalPlayers) {
            int roll = rollDice(6);
            playerThread.out.println("Hai lanciato un dado da spareggio: " + roll);
            broadcast(playerThread.player.getName() + " ha lanciato un " + roll + " nello spareggio.");

            if (roll > highestRoll) {
                highestRoll = roll;
                winner = playerThread;
            }
        }

        if (winner != null) {
            broadcast(winner.player.getName() + " ha vinto la partita con un lancio di " + highestRoll + "!");
            endGame();
        }
    }

    // Metodo per terminare la partita e disconnettere i client
    private void endGame() {
        broadcast("La partita è terminata. Grazie per aver giocato!");

        // Disconnette tutti i giocatori
        synchronized (playersList) {
            for (GameClientThread playerThread : playersList) {
                try {
                    playerThread.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            playersList.clear();
            finalPlayers.clear();
        }
    }

    // Metodo per inizializzare il tabellone
    private static void initializeBoard() {
        for (int i = 0; i < board.length - 1; i++) {
            int probability = random.nextInt(100) + 1;
            if (i == 0) {
                System.out.println("Zona Iniziale");
                board[i] = new Cell(ZoneType.REST, "Zona Iniziale");
            } else if (probability <= 40) {
                board[i] = new Cell(ZoneType.NEUTRAL, "Zona Neutra");
            } else if (probability <= 60) {
                board[i] = new Cell(ZoneType.REST, "Zona Riposo ");
            } else if (probability <= 80) {
                board[i] = new Cell(ZoneType.HOSTILE, "Zona Ostile");
            } else {
                board[i] = new Cell(ZoneType.TREASURE, "Zona Tesoro");
            }
        }
        board[board.length - 1] = new Cell(ZoneType.FINAL, "Zona Finale");
    }

    // Permette di lanciare un dado
    private static int rollDice(int sides) {
        return random.nextInt(sides) + 1;
    }

    // Invio di un messaggio a tutti i giocatori
    private void broadcast(String message) {
        synchronized (playersList) {
            for (GameClientThread playerThread : playersList) {
                playerThread.out.println(message);
            }
        }
    }
}
