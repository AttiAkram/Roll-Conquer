package com.example.rollconquer;

import java.io.*;
import java.net.Socket;

public class ServerInit {
    private String serverAddress;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerInit(String serverAddress, int port) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.socket = new Socket(serverAddress, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void startListening() {
        new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                }
            } catch (IOException e) {
                System.out.println("Connessione chiusa.");
            }
        }).start();
    }

    public Socket getSocket() {
        return socket;
    }
}
