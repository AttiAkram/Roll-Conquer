package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class GameClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private static final Random random = new Random();
    private static int turnCounter = 0;

    public static final ArrayList<GameClientThread> playersList = new ArrayList<>();


    public GameClientThread(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Errore inizializzazione GameClientThread", e);
        }
    }

    @Override
    public void run() {
        try {
            // Ricevi il nome del giocatore all'inizio
            playerName = in.readLine();
            System.out.println("Nuovo giocatore connesso: " + playerName);
            broadcast(playerName + " si è unito alla partita.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(playerName + ": " + message);
                broadcast(playerName + ": " + message);
            }
        } catch (IOException e) {
            System.out.println(playerName + " si è disconnesso.");
        } finally {
            synchronized (playersList) {
                playersList.remove(this);
            }
        }
    }


    private void broadcast(String message) {
        synchronized (playersList) {
            for (GameClientThread player : playersList) {
                player.out.println(message);
            }
        }
    }

    // permette di lanciare un dado
    static int rollDice(int sides) {
        return random.nextInt(sides) + 1;
    }

}
