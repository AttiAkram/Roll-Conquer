package com.example.rollconquer;

import java.net.Socket;
import java.util.Random;

public class ServerGame extends AbstractServer {
    private static final int BOARD_SIZE = 100;
    private static final Cell[] board = new Cell[BOARD_SIZE];
    private static final Random random = new Random();


    // Inizializza il tabellone
    private static void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE - 1; i++) {
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
        board[BOARD_SIZE - 1] = new Cell(ZoneType.FINAL, "Zona Finale");
    }

    public ServerGame(int port) {
        super(port);
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        GameClientThread gameClientThread = new GameClientThread(clientSocket);
        synchronized (GameClientThread.playersList) {
            GameClientThread.playersList.add(gameClientThread);
        }
        gameClientThread.start();
    }

    public static void main(String[] args) {
        new ServerGame(12346).startServer();
    }
}

