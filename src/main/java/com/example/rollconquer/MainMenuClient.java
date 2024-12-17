package com.example.rollconquer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainMenuClient extends Application {

    private TextArea chatArea;     // Area per visualizzare i messaggi
    private TextField inputField;  // Campo per scrivere i messaggi
    private Button connectButton;  // Pulsante per inviare il comando "pronto"
    private PrintWriter out;       // Stream per inviare messaggi al server
    private BufferedReader in;     // Stream per ricevere messaggi dal server
    private Socket socket;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client - Main Menu");

        // **Area Chat**: Visualizzazione messaggi
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        // **Campo Input e Pulsante Invia**
        inputField = new TextField();
        inputField.setPromptText("Scrivi un messaggio...");
        Button sendButton = new Button("Invia");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));

        // **Pulsante Connetti alla Partita**
        connectButton = new Button("Connetti alla Partita");
        connectButton.setOnAction(e -> sendReadySignal());
        VBox connectBox = new VBox(10, new Label("Main Menu"), connectButton);
        connectBox.setPadding(new Insets(10));

        // **Layout Principale**
        BorderPane root = new BorderPane();
        root.setCenter(chatArea);
        root.setBottom(inputBox);
        root.setRight(connectBox);

        // Configurazione della scena
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Connessione al server
        connectToServer();
    }

    // Metodo per connettersi al server
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); // Indirizzo del server e porta
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Thread per ricevere messaggi dal server
            new Thread(() -> {
                try {

                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        String finalMessage = serverMessage;
                        Platform.runLater(() -> chatArea.appendText("Server: " + finalMessage + "\n"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


            chatArea.appendText("Connesso al server!\n");
        } catch (Exception e) {
            chatArea.appendText("Errore di connessione al server!\n");
            e.printStackTrace();
        }
    }

    // Metodo per inviare messaggi normali
    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message); // Invia il messaggio al server
            chatArea.appendText("Tu: " + message + "\n");
            inputField.clear();
        }

    }

    // Metodo per inviare il comando "pronto"
    private void sendReadySignal() {
        out.println("pronto");
        chatArea.appendText("Hai inviato il comando: pronto\n");
        connectButton.setDisable(true); // Disabilita il pulsante dopo averlo premuto
    }

    @Override
    public void stop() throws Exception {
        // Chiusura delle risorse
        if (socket != null) socket.close();
        if (out != null) out.close();
        if (in != null) in.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
