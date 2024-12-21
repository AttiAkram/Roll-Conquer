package com.example.rollconquer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static String clientName;
    private static PrintWriter serverOut; // Per inviare al server principale
    private static PrintWriter gameOut;   // Per inviare al ServerGame


    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("Connesso al server!");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Thread per ricevere messaggi dal server
            Thread receiveThread = new Thread(() -> {
                String serverMessage;
                try {
                    String prefix = "Benvenuto";
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Messaggio dal server: " + serverMessage);

                        if (serverMessage.equals("Tutti pronti! Connettiti al ServerGame sulla porta 12346 per iniziare il gioco.")) {
                            connectToServerGame(clientName);
                            break;
                        } else if (serverMessage.startsWith(prefix)) {
                            clientName = serverMessage.substring(prefix.length()).trim();
                            System.out.println("Client name: " + clientName);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connessione chiusa dal server.");
                }
            });

            receiveThread.start(); // Avvia il thread di ricezione

            // Thread unificato per inviare comandi al server principale o al ServerGame
            Thread sendThread = new Thread(() -> {
                String userInput;
                try {
                    while ((userInput = consoleInput.readLine()) != null) {
                        if (userInput.startsWith("chat:")) {
                            // Invio al server principale
                            String message = userInput.substring(5).trim();
                            serverOut.println(message);
                        } else if (userInput.startsWith("game:") && gameOut != null) {
                            // Invio al ServerGame
                            String command = userInput.substring(5).trim();
                            gameOut.println(command);
                        } else {
                            System.out.println("Formato comando non valido! Usa 'chat:' o 'game:' oppure non ancora unito al server game.");
                        }

                        if (userInput.equalsIgnoreCase("exit")) {
                            System.out.println("Disconnessione in corso...");
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sendThread.start(); // Avvia il thread di invio

            receiveThread.join();
            sendThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void connectToServerGame(String clientName) {
        try {
            Socket gameSocket = new Socket("localhost", 12346);
            System.out.println("Connesso al ServerGame!");

            BufferedReader in = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
            gameOut = new PrintWriter(gameSocket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Invia il nome del client al ServerGame
            gameOut.println(clientName);

            // Thread per ricevere messaggi dal ServerGame
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
            gameReceiveThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println("Errore di connessione al ServerGame.");
        }
    }
}
