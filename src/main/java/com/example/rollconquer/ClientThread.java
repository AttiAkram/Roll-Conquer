package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientThread extends Thread {
    private static final List<String> FANTASY_NAMES = Arrays.asList(
            "Dracowolf", "Shadowfox", "Phoenixcat", "Gryphondog",
            "Moonhare", "Stormserpent", "Frostlynx", "Emberstag",
            "Crystalotter", "Thunderowl"
    );

    private static int nameIndex = 0;
    private String clientName;
    private boolean isReady = false; // Stato pronto del client

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public static final ArrayList<ClientThread> clientsList = new ArrayList<>();



    private boolean hasRolled = false; // Indica se il client ha lanciato i dadi nella fase corrente
    private int lastRoll = 0;

    public ClientThread(Socket socket) {
        this.socket = socket;

        synchronized (FANTASY_NAMES) {
            if (nameIndex >= FANTASY_NAMES.size()) {
                nameIndex = 0; // Ricomincia se i nomi sono finiti
            }
            this.clientName = FANTASY_NAMES.get(nameIndex++);
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            out.println("Benvenuto " + clientName + "! Digita 'pronto' per dichiararti pronto.");
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
                    System.out.println(clientName + " è pronto.");
                    out.println("Sei stato dichiarato pronto!");
                    checkGameStart();
                } else {
                    System.out.println(clientName + ": " + line);
                    broadcast(clientName + ": " + line);
                }
            }
        } catch (IOException e) {
            System.out.println(clientName + " si è disconnesso inaspettatamente.");
        }
    }


    private void notifyAllClients(String message) {
        synchronized (clientsList) {
            for (ClientThread ct : clientsList) {
                ct.out.println(message);
            }
        }
    }

    private void checkGameStart() {
        synchronized (clientsList) {
            long readyCount = clientsList.stream().filter(ct -> ct.isReady).count();
            if (readyCount == clientsList.size()) {
                System.out.println("Tutti i client sono pronti. La partita inizia!");
                notifyAllClients("La partita è iniziata! Preparati per il lancio dei dadi.");
                while (true) {
                    if (clientsList.stream().anyMatch(ct -> !ct.hasRolled())) {
                        for (ClientThread ct : clientsList) {
                            if (!ct.hasRolled()) {
                                ct.rollDice(); // Lancia i dadi
                                System.out.println(ct.getClientName() + " ha lanciato i dadi: " + ct.getLastRoll());
                            }
                        }
                    } else {
                        break;
                    }
                    Server.phase1RollDice(); // Chiama il metodo per il lancio dei dadi

                }
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public int rollDice() {
        // Simula il lancio di due dadi da 6
        int dice1 = (int) (Math.random() * 6) + 1;
        int dice2 = (int) (Math.random() * 6) + 1;
        lastRoll = dice1 + dice2;
        hasRolled = true; // Segna che il client ha lanciato i dadi
        return lastRoll;
    }

    public boolean hasRolled() {
        return hasRolled;
    }

    public void resetRoll() {
        hasRolled = false;
    }

    public int getLastRoll() {
        return lastRoll;
    }

    public String getClientName() {
        return clientName;
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
}
