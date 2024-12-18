package com.example.rollconquer;

import java.net.Socket;

public class Server extends AbstractServer {
    public Server(int port) {
        super(port);
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        ClientThread clientThread = new ClientThread(clientSocket);
        synchronized (ClientThread.clientsList) {
            ClientThread.clientsList.add(clientThread);
        }
        clientThread.start();
    }

    public static void main(String[] args) {
        new Server(12345).startServer();
    }
}
