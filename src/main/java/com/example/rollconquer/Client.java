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
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Messaggio dal server: " + serverMessage);
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
}
