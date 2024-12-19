package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class GameClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Player player; // Oggetto Player per ogni client
    private static final Random random = new Random();

    private boolean hasThrow;

    public static final ArrayList<GameClientThread> playersList = new ArrayList<>();
    private static final Cell[] board = new Cell[100]; // Simuliamo la board di gioco

    // Costruttore
    public GameClientThread(Socket socket) {
        this.socket = socket;

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
                hasThrow = false;
                if (message.equalsIgnoreCase("lancia") && isHasThrowAll()) {
                    handleDiceRoll();
                    hasThrow = true;
                } else if (message.equalsIgnoreCase("info")) {
                    player.showInfo();
                } else if(!isHasThrowAll()) {
                    out.println("Aspetta che tutti i giocatori lancino i dadi.");
                }else {
                    out.println("comando non valido, usare lancia o info.");
                    //System.out.println(player.getName() + ": " + message);
                    //broadcast(player.getName() + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println(player.getName() + " si è disconnesso.");
        } finally {
            synchronized (playersList) {
                playersList.remove(this);
            }
            broadcast(player.getName() + " ha lasciato la partita.");
        }
    }

    private boolean isHasThrowAll() {
        for (GameClientThread playerThread : playersList) {
            if (!playerThread.hasThrow) { // Controlla se un giocatore NON ha ancora lanciato
                return false;             // Se almeno uno NON ha lanciato, ritorna false
            }
        }
        return true; // Se tutti hanno lanciato, ritorna true
    }


    // Metodo per gestire il lancio dei dadi
    private void handleDiceRoll() {
        int diceRoll = rollDice(6); // Lancia un dado a 6 facce
        int movement = player.calculateMovement(diceRoll); // Calcola il movimento
        player.move(movement, board); // Muove il giocatore sulla board

        // Invio delle informazioni al client
        out.println("Hai lanciato un " + diceRoll + ".");
        out.println("Nuova posizione: " + player.getPosition());

        // Mostra informazioni aggiornate
        broadcast(player.getName() + " ha lanciato un " + diceRoll + " e ora è in posizione " + player.getPosition());
    }

    // Metodo per inizializzare il tabellone
    private static void initializeBoard() {
        for (int i = 0; i < board.length - 1; i++) {
            int probability = random.nextInt(100) + 1;
            if (probability <= 40) {
                board[i] = new Cell(ZoneType.NEUTRAL, "Zona Neutra");
            } else if (probability <= 60) {
                board[i] = new Cell(ZoneType.REST, "Zona Riposo");
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
