package com.example.rollconquer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class GameClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Game game;
    private Player player;

    public static final ArrayList<GameClientThread> playersList = new ArrayList<>();

    public GameClientThread(Socket socket, Game game) {
        this.socket = socket;
        this.game = game;


        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            System.out.println("Errore nel client thread.");
        }
    }

    @Override
    public void run() {



    }

    private void broadcast(String message) {
        synchronized (playersList) {
            for (GameClientThread player : playersList) {
                player.out.println(message);
            }
        }
    }
}
