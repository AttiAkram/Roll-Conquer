package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientThread extends Thread {
    private static final List<String> FANTASY_NAMES = new ArrayList<>(Arrays.asList(
            "Dracowolf", "Shadowfox", "Phoenixcat", "Gryphondog",
            "Moonhare", "Stormserpent", "Frostlynx", "Emberstag",
            "Crystalotter", "Thunderowl"
    ));

    private static int nameIndex = 0;
    private String clientName;
    private boolean isReady = false; // Stato pronto del client
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public static final ArrayList<ClientThread> clientsList = new ArrayList<>();

    public ClientThread(Socket socket) {
        this.socket = socket;

        synchronized (FANTASY_NAMES) {
            if (nameIndex >= FANTASY_NAMES.size()) {
                nameIndex = 0; // Reset e rimischia quando finisce la lista
                shuffleNames();
            }
            this.clientName = FANTASY_NAMES.get(nameIndex++);

        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            out.println("Benvenuto " + clientName);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {

        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("exit")) {
                    System.out.println(clientName + " si è disconnesso.");
                    broadcast(clientName + " si è disconnesso.");
                    synchronized (clientsList) {
                        clientsList.remove(this);
                    }
                    socket.close();
                    break;
                } else if (line.equalsIgnoreCase("pronto")) {
                    this.isReady = true;
                    out.println("Sei stato dichiarato pronto!");
                    System.out.println(clientName + " è pronto.");
                    broadcast(clientName + " è pronto.");
                    checkGameStart();
                } else {
                    System.out.println(clientName + ": " + line);
                    broadcast(clientName + ": " + line);
                }
            }
        } catch (IOException e) {
            synchronized (clientsList) {
                clientsList.remove(this);
            }
            System.out.println(clientName + " si è disconnesso inaspettatamente.");

        }
    }


    private void checkGameStart() {
        synchronized (clientsList) {
            long readyCount = clientsList.stream().filter(ct -> ct.isReady).count();
            if (readyCount == clientsList.size()) {
                //System.out.println("Tutti i client sono pronti. Avvio del ServerGame...");
                //System.out.println("client name: " + this.clientName);
                notifyAllClients("Tutti pronti! Connettiti al ServerGame sulla porta 12346 per iniziare il gioco.");


            }
        }
    }


    public void sendMessage(String message) {
        out.println(message);
    }


    private void broadcast(String message) {
        synchronized (clientsList) {
            for (ClientThread client : clientsList) {
                if (client != this) { // Non invia il messaggio al mittente
                    client.out.println(message);
                }
            }
        }
    }

    private static void notifyAllClients(String message) {

        synchronized (ClientThread.clientsList) {
            for (ClientThread ct : ClientThread.clientsList) {
                ct.sendMessage(message);
            }
        }
    }


    private void shuffleNames() {
        Collections.shuffle(FANTASY_NAMES);
    }
}
