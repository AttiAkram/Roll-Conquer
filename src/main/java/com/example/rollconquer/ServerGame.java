package com.example.rollconquer;

import java.io.*;
import java.net.*;

public class ServerGame {


    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12346);
            while (true) {
                Socket s = serverSocket.accept();
                GameClientThread ct = new GameClientThread(s);
                synchronized (GameClientThread.playersList) {
                    GameClientThread.playersList.add(ct);
                }
                ct.start();
                System.out.println("Client connesso! ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}


