package com.example.rollconquer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainMenuClient extends Application {

    private TextArea chatArea;     // Area per visualizzare i messaggi
    private TextField inputField;  // Campo per scrivere i messaggi
    private PrintWriter out;       // Stream per inviare messaggi al server
    private BufferedReader in;     // Stream per ricevere messaggi dal server
    private Socket socket;
    private double x = 162;        // Variabile per la posizione X del pulsante
    private double y = 304;        // Variabile per la posizione Y del pulsante

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client - Main Menu");

        // **Area Chat**: Visualizzazione messaggi
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefSize(115, 177); // Imposta la dimensione dell'area chat
        chatArea.setLayoutX(570); // Imposta la posizione X
        chatArea.setLayoutY(132); // Imposta la posizione Y
        chatArea.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;"); // Imposta il colore del testo a nero e lo sfondo a bianco

        // **Campo Input e Pulsante Invia**
        inputField = new TextField();
        inputField.setPromptText("Scrivi un messaggio...");
        inputField.setPrefWidth(115); // Imposta la larghezza del campo di input per essere uguale all'area della chat
        javafx.scene.control.Button sendButton = new javafx.scene.control.Button("Invia");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setPrefWidth(115); // Imposta la larghezza del pulsante per essere uguale all'area della chat

        // VBox per centrare il pulsante sotto il campo di input
        VBox inputBox = new VBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setLayoutX(560); // Imposta la posizione X
        inputBox.setLayoutY(309); // Imposta la posizione Y

        // **Immagine del Pulsante Connetti alla Partita**
        Image buttonImage = new Image(getClass().getResourceAsStream("/images/button.png"));
        ImageView buttonImageView = new ImageView(buttonImage);
        buttonImageView.setFitWidth(156);
        buttonImageView.setFitHeight(52);
        buttonImageView.setLayoutX(x); // Imposta la posizione X iniziale
        buttonImageView.setLayoutY(y); // Imposta la posizione Y iniziale

        buttonImageView.setOnMouseClicked(e -> {
            sendReadySignal();
            buttonImageView.setLayoutY(buttonImageView.getLayoutY() + 5); // Sposta il pulsante giù di 5 pixel
        });

        // **Layout Principale**
        Pane root = new Pane();
        root.getChildren().addAll(chatArea, inputBox, buttonImageView);

        // **Imposta l'immagine di sfondo**
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
        javafx.scene.layout.BackgroundImage background = new javafx.scene.layout.BackgroundImage(backgroundImage, javafx.scene.layout.BackgroundRepeat.NO_REPEAT, javafx.scene.layout.BackgroundRepeat.NO_REPEAT, javafx.scene.layout.BackgroundPosition.DEFAULT, javafx.scene.layout.BackgroundSize.DEFAULT);
        root.setBackground(new javafx.scene.layout.Background(background));

        // Configurazione della scena
        Scene scene = new Scene(root, 791, 437);
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