package com.example.rollconquer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Indirizzo del server
        int port = 12345; // Porta del server

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connesso al server!");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Thread per ricevere i messaggi dal server
            Thread receiveThread = new Thread(() -> {
                String serverMessage;
                try {
                    String prefix = "Benvenuto";
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Messaggio dal server: " + serverMessage);

                        if (serverMessage.equals("Tutti pronti! Connettiti al ServerGame sulla porta 12346 per iniziare il gioco.")) {

                            connectToServerGame(serverMessage);
                            break;
                        } else if (serverMessage.startsWith(prefix)) {
                            String clientName = serverMessage.substring(prefix.length()).trim();
                            System.out.println("Client name: " + clientName);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connessione chiusa dal server.");
                }
            });


            receiveThread.start(); // Avvia il thread di ricezione

            // Thread per inviare i messaggi al server
            Thread sendThread = new Thread(() -> {
                String userInput;
                try {
                    while ((userInput = consoleInput.readLine()) != null) {
                        out.println(userInput); // Invia il messaggio al server
                        if (userInput.equalsIgnoreCase("exit")) {
                            System.out.println("Disconnessione in corso...");
                            break;
                        }
                    }
                    socket.close(); // Chiudi il socket se l'utente scrive "exit"
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sendThread.start(); // Avvia il thread di invio

            // Aspetta che i due thread terminino prima di chiudere il client
            receiveThread.join();
            sendThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void connectToServerGame(String clientName) {
        try (Socket gameSocket = new Socket("localhost", 12346)) {
            System.out.println("Connesso al ServerGame!");
            BufferedReader in = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
            PrintWriter out = new PrintWriter(gameSocket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Thread per ricevere i messaggi dal ServerGame
            Thread gameReceiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("ServerGame: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connessione chiusa dal ServerGame.");
                }
            });
            gameReceiveThread.start();

            // Thread per inviare i messaggi al serverGame
            Thread sendThread = new Thread(() -> {
                String userInput;
                try {
                    while ((userInput = consoleInput.readLine()) != null) {
                        out.println(userInput); // Invia il messaggio al server
                        if (userInput.equalsIgnoreCase("exit")) {
                            System.out.println("Disconnessione in corso...");
                            break;
                        }
                    }
                   // Chiudi il socket se l'utente scrive "exit"
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sendThread.start(); // Avvia il thread di invio

            gameSocket.close();
        } catch (IOException e) {
            System.out.println("Errore di connessione al ServerGame.");
        }
    }

}

