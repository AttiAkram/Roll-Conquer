package com.example.rollconquer;

import java.net.Socket;

public class ServerGame extends AbstractServer {
    private Game game;

    public ServerGame(int port) {
        super(port);
        this.game = new Game(); // Inizializza l'istanza del gioco
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        GameClientThread gameClientThread = new GameClientThread(clientSocket, game);
        synchronized (GameClientThread.playersList) {
            GameClientThread.playersList.add(gameClientThread);
        }
        gameClientThread.start();
    }

    public static void main(String[] args) {
        new ServerGame(12346).startServer();
    }
}

