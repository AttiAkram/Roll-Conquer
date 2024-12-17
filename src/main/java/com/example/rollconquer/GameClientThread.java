package com.example.rollconquer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class GameClientThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    public static final ArrayList<GameClientThread> playersList = new ArrayList<GameClientThread>();
    public GameClientThread(Socket socket) {
        this.socket = socket;
        this.playerName = "benve";
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Benvenuto " + this.playerName + "! Digita 'pronto' per dichiararti pronto.");
            synchronized (GameClientThread.playersList) {
                GameClientThread.playersList.add(this);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void run() {


    }

    public void sendMessage(String message) {
        out.println(message);
    }




    private static void notifyAllClients(String message) {

        synchronized (GameClientThread.playersList) {
            for (GameClientThread ct : GameClientThread.playersList) {
                ct.sendMessage(message);
            }
        }
    }
}