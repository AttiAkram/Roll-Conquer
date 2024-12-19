package com.example.rollconquer;

import java.net.Socket;

public class ServerGame extends AbstractServer {

    protected int loopCount = 1;

    public ServerGame(int port) {
        super(port);
    }
    @Override
    protected void handleClient(Socket clientSocket) {
        GameClientThread gameClientThread = new GameClientThread(this, clientSocket);
        synchronized (GameClientThread.playersList) {
            GameClientThread.playersList.add(gameClientThread);
        }
        gameClientThread.start();
    }
    public static void main(String[] args) {
        new ServerGame(12346).startServer();
    }
}