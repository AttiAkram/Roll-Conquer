package com.example.rollconquer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int MAX_CLIENTS = 5; // numero massimo di client che possono connettersi al server

    public static void main(String[] args) {
        // quando viene eseguito il programma si crea un oggetto socket  (ServerSocket) in ascolto sulla porta 12345
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            while (true) {

                Socket s = serverSocket.accept(); // il metodo accept() blocca il codice fino a quando un client si collega
                // dopo che un client si è connesso, restituisce un oggetto Socket e viene creato un oggetto ClientThread e aggiunto alla lista dei client


                // Controlla se è stato raggiunto il limite massimo di client
                if (ClientThread.clientsList.size() >= MAX_CLIENTS) {
                    System.out.println("Connessione rifiutata: troppi client connessi.");
                    s.close(); // Chiudi il socket per il client non accettato
                    continue;
                    //Il comando continue forza il ciclo while a saltare tutto il codice successivo e passare direttamente alla prossima iterazione.
                }

                ClientThread ct = new ClientThread(s);
                //per la gestione di piu client si crea un oggetto ClientThread che contiene una lista di tutti i client collegati asociando per ogni client un thread


                // ClientThread.clientsList.add(ct); // qua viene aggiunto alla lista di tipo static
                synchronized (ClientThread.clientsList) {
                    ClientThread.clientsList.add(ct);
                } // per evitare problemi di concorrenza si usa synchronized

                ct.start(); // e poi viene avviato il thread
                System.out.println("Client connesso! ");

                // in fine viene ripetuto il codice allinfinito per gestire piu client,
                //quindi il serevr rimane in ascolto e tramite il metoto accept() si mette in attesa di un client nuovo  bloccando il codice
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}