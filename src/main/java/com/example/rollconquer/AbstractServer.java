package com.example.rollconquer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractServer {
    protected int port;

    public AbstractServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server in ascolto sulla porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione accettata: " + clientSocket);

                handleClient(clientSocket); // Metodo astratto
            }
        } catch (IOException e) {
            System.err.println("Errore del server: " + e.getMessage());
        }
    }

    protected abstract void handleClient(Socket clientSocket);
}
