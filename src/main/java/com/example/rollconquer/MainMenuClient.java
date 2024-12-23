package com.example.rollconquer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainMenuClient extends Application {

    private TextArea chatArea;
    private TextField inputField;
    private PrintWriter serverOut;
    private PrintWriter gameOut;
    private BufferedReader serverIn;
    private Socket socket;
    private String clientName;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client - Main Menu");

        // Area Chat con sfondo immagine
        Image chatBackgroundImage = new Image(getClass().getResourceAsStream("/images/textureWoodChat.jpg"));
        ImageView chatBackgroundView = new ImageView(chatBackgroundImage);
        chatBackgroundView.setFitWidth(173); // Larghezza
        chatBackgroundView.setFitHeight(255); // Altezza
        chatBackgroundView.setLayoutX(540); // Posizione X
        chatBackgroundView.setLayoutY(54); // Posizione Y

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefSize(173, 255);
        chatArea.setLayoutX(540); // Deve coincidere con l'immagine
        chatArea.setLayoutY(54); // Deve coincidere con l'immagine

        // Imposta lo stile per rendere trasparente lo sfondo e i bordi
        chatArea.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-background-radius: 0; " +
                "-fx-control-inner-background: transparent; " +
                "-fx-text-fill: black; " +
                "-fx-border-color: transparent;");
        chatArea.setOpacity(0.8); // Per garantire un livello di trasparenza

        // Aggiungi prima lo sfondo (ImageView) e poi la TextArea


        // Campo Input e Pulsante Invia
        inputField = new TextField();
        inputField.setPromptText("Scrivi un messaggio...");
        inputField.setPrefWidth(115);

        Image sendImage = new Image(getClass().getResourceAsStream("/images/invia.png"));
        ImageView sendImageView = new ImageView(sendImage);
        sendImageView.setFitWidth(122);
        sendImageView.setFitHeight(39);
        sendImageView.setOnMouseClicked(e -> sendMessage());

        VBox inputBox = new VBox(10, inputField, sendImageView);
        inputBox.setPadding(new Insets(10));
        inputBox.setLayoutX(554);
        inputBox.setLayoutY(313);

        // Pulsante "Pronto"
        Image buttonImage = new Image(getClass().getResourceAsStream("/images/button.png"));
        ImageView buttonImageView = new ImageView(buttonImage);
        buttonImageView.setFitWidth(156);
        buttonImageView.setFitHeight(52);
        buttonImageView.setLayoutX(162);
        buttonImageView.setLayoutY(304);

        buttonImageView.setOnMouseClicked(e -> {
            sendMessage("chat: pronto"); // Invia automaticamente "chat: pronto"
            chatArea.appendText("Tu: pronto\n");
        });

        // Pulsante "Rule"
        Image buttonImageRule = new Image(getClass().getResourceAsStream("/images/Rules.png"));
        ImageView buttonImageRuleView = new ImageView(buttonImageRule);
        buttonImageRuleView.setFitWidth(32);
        buttonImageRuleView.setFitHeight(31);
        buttonImageRuleView.setLayoutX(47);
        buttonImageRuleView.setLayoutY(360);

        buttonImageRuleView.setOnMouseClicked(e -> {
            // Crea un nuovo Stage (finestra)
            Stage rulesWindow = new Stage();
            rulesWindow.setTitle("Regole");

            // Imposta le dimensioni della finestra
            rulesWindow.setWidth(437);
            rulesWindow.setHeight(791);

            // Crea un pannello per il contenuto
            Pane root = new Pane();

            // Carica l'immagine di sfondo
            BackgroundImage backgroundImage = new BackgroundImage(
                    new Image(getClass().getResourceAsStream("/images/RulesBg.png"), 437, 791, false, true),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT
            );
            root.setBackground(new Background(backgroundImage));

            // Aggiungi il pannello a una scena
            Scene scene = new Scene(root, 437, 791);

            // Imposta la scena sulla finestra
            rulesWindow.setScene(scene);

            // Rendi la finestra chiudibile
            rulesWindow.initModality(Modality.APPLICATION_MODAL); // Blocca interazioni con la finestra principale
            rulesWindow.show();
        });

        // Immagine di sfondo
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(791);
        backgroundView.setFitHeight(437);

        // Layout principale
        Pane root = new Pane();
        root.getChildren().addAll(backgroundView, inputBox, buttonImageView, chatBackgroundView, chatArea, buttonImageRuleView);
        // Aggiungi prima l'immagine di sfondo, poi la TextArea

        Scene scene = new Scene(root, 791, 437);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer(root, backgroundView, buttonImageView);
    }

    private void connectToServer(Pane root, ImageView backgroundView, ImageView buttonImageView) {
        try {
            socket = new Socket("localhost", 12345);
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverMessage;
                    String prefix = "Benvenuto";
                    while ((serverMessage = serverIn.readLine()) != null) {
                        String finalMessage = serverMessage;
                        Platform.runLater(() -> chatArea.appendText("Server: " + finalMessage + "\n"));

                        if (serverMessage.startsWith(prefix)) {
                            clientName = serverMessage.substring(prefix.length()).trim();
                        } else if (serverMessage.contains("Connettiti al ServerGame sulla porta 12346")) {
                            connectToServerGame(clientName, root, backgroundView, buttonImageView);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            Platform.runLater(() -> chatArea.appendText("Connesso al server!\n"));
        } catch (Exception e) {
            Platform.runLater(() -> chatArea.appendText("Errore di connessione al server!\n"));
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            sendMessage(message);
            inputField.clear();
        }
    }

    private void sendMessage(String message) {
        if (message.startsWith("chat:")) {
            chatArea.appendText("Tu: " + message.substring(5).trim() + "\n");
            serverOut.println(message.substring(5).trim());

        } else if (message.startsWith("game:") && gameOut != null) {
            gameOut.println(message.substring(5).trim());
        } else {
            chatArea.appendText("Formato comando non valido! Usa 'chat:' o 'game:'.\n");
        }
    }

    private void connectToServerGame(String clientName, Pane root, ImageView backgroundView, ImageView buttonImageView) {
        try {
            // Connessione al ServerGame
            Socket gameSocket = new Socket("localhost", 12346);
            gameOut = new PrintWriter(gameSocket.getOutputStream(), true);
            BufferedReader gameIn = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));

            gameOut.println(clientName);
            chatArea.appendText("Connesso al ServerGame!\n");


            // Pulsante "Lancia"
            Image buttonDiceImage = new Image(getClass().getResourceAsStream("/images/ThrowDice!.png"));
            ImageView buttonDiceImageView = new ImageView(buttonDiceImage);
            buttonDiceImageView.setFitWidth(174);
            buttonDiceImageView.setFitHeight(67);
            buttonDiceImageView.setLayoutX(21);
            buttonDiceImageView.setLayoutY(350);


            // Creazione delle Label vuote
            Label labelPrimoLancio = new Label();
            labelPrimoLancio.setLayoutX(298);
            labelPrimoLancio.setLayoutY(323);
            labelPrimoLancio.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            Label labelSecondoLancio = new Label();
            labelSecondoLancio.setLayoutX(388);
            labelSecondoLancio.setLayoutY(323);
            labelSecondoLancio.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            Label labelMalus = new Label();
            labelMalus.setLayoutX(150);
            labelMalus.setLayoutY(62);
            labelMalus.setStyle("-fx-font-size: 18px; -fx-text-fill: #000000;");

            Label labelBonus = new Label();
            labelBonus.setLayoutX(150);
            labelBonus.setLayoutY(28);
            labelBonus.setStyle("-fx-font-size: 18px; -fx-text-fill: #000000;");

            Label labelPosizione = new Label();
            labelPosizione.setLayoutX(345);
            labelPosizione.setLayoutY(207);
            labelPosizione.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            Label labelZona = new Label();
            labelZona.setLayoutX(62);
            labelZona.setLayoutY(212);
            labelZona.setStyle("-fx-font-size: 18px; -fx-text-fill: #000000;");

            Label labelTurni = new Label();
            labelTurni.setLayoutX(54);
            labelTurni.setLayoutY(43);
            labelTurni.setStyle("-fx-font-size: 18px; -fx-text-fill: #000000;");

            TextArea playerArea = new TextArea();
            playerArea.setEditable(false);
            playerArea.setWrapText(true);
            playerArea.setPrefSize(174, 176);
            playerArea.setLayoutX(258); // Deve coincidere con l'immagine
            playerArea.setLayoutY(119); // Deve coincidere con l'immagine

            // Imposta lo stile per rendere trasparente lo sfondo e i bordi
            playerArea.setStyle("-fx-background-color: transparent; " +
                    "-fx-background-insets: 0; " +
                    "-fx-background-radius: 0; " +
                    "-fx-control-inner-background: transparent; " +
                    "-fx-text-fill: black; " +
                    "-fx-border-color: transparent;");
            playerArea.setOpacity(0.8); // Per garantire un livello di trasparenza


            // Cambia il background e rimuovi il pulsante "Pronto" dopo la connessione
            Platform.runLater(() -> {
                // Cambia l'immagine di sfondo
                Image mainImage = new Image(getClass().getResourceAsStream("/images/main.png"));
                backgroundView.setImage(mainImage);

                // Rimuovi il pulsante "Pronto" dalla scena
                root.getChildren().remove(buttonImageView);
                root.getChildren().add(buttonDiceImageView);
                root.getChildren().addAll(playerArea,labelPrimoLancio, labelSecondoLancio, labelMalus, labelBonus, labelPosizione, labelZona, labelTurni);

            });


            buttonDiceImageView.setOnMouseClicked(e -> {
                sendMessage("game: lancia"); // Invia automaticamente "game: lancia"
            });
            // Thread per ricevere messaggi dal ServerGame
            new Thread(() -> {
                String primoLancio = "";
                String secondoLancio = "";
                String malus = "";
                String bonus = "";
                String posizione = "";
                String zona = "";
                String turni = "";

                labelPrimoLancio.setText(primoLancio);
                labelSecondoLancio.setText(secondoLancio);
                labelMalus.setText(malus);
                labelBonus.setText(bonus);
                labelPosizione.setText(posizione);
                labelZona.setText(zona);
                labelTurni.setText(turni);
                try {
                    String gameMessage;
                    while ((gameMessage = gameIn.readLine()) != null) {
                        String finalGameMessage = gameMessage;

                        // Verifica i messaggi ricevuti e aggiorna le variabili di gioco
                        if (finalGameMessage.contains("Hai la prima volta lanciato")) {
                            // Estrai il valore del primo lancio
                            primoLancio = finalGameMessage.split(" ")[6]; // supponendo che il numero del lancio sia sempre al 6° indice
                            String finalPrimoLancio = primoLancio;
                            Platform.runLater(() ->  labelPrimoLancio.setText(finalPrimoLancio));
                        } else if (finalGameMessage.contains("Hai la seconda volta lanciato")) {
                            // Estrai il valore del secondo lancio
                            secondoLancio = finalGameMessage.split(" ")[6]; // supponendo che il numero del lancio sia sempre al 6° indice
                            String finalSecondoLancio = secondoLancio;
                            Platform.runLater(() -> labelSecondoLancio.setText(finalSecondoLancio));
                        } else if (finalGameMessage.contains("ha lanciato un")) {
                            // Estrai la nuova posizione
                            posizione = finalGameMessage;
                            String finalPosizione = posizione;
                            //Platform.runLater(() -> labelPosizione.setText(finalPosizione ));
                            Platform.runLater(() -> playerArea.appendText(finalPosizione + "\n"));
                        } else if (finalGameMessage.contains("Bonus Attuale:")) {
                            // Estrai il bonus attuale
                            bonus = finalGameMessage.split(": ")[1].split("\n")[0];
                            String finalBonus = bonus;
                            Platform.runLater(() -> labelBonus.setText(finalBonus));
                        } else if (finalGameMessage.contains("Malus Attuale:")) {
                            // Estrai il malus attuale
                            malus = finalGameMessage.split(": ")[1].split("\n")[0];
                            String finalMalus = malus;
                            Platform.runLater(() -> labelMalus.setText(finalMalus));
                        } else if (finalGameMessage.contains("Zona")) {
                            // Aggiorna la zona (esempio di zona specifica)
                            zona = finalGameMessage;
                            String finalZona = zona;
                            Platform.runLater(() -> labelZona.setText(finalZona));
                        } else if (finalGameMessage.contains("Turni ")) {
                            // Aggiorna i turni
                            turni = finalGameMessage.replace("Turni ", "").trim();
                            String finalTurni = turni;
                            Platform.runLater(() -> labelTurni.setText(finalTurni));
                        } else {
                            // Invia solo il messaggio generico alla chat
                            Platform.runLater(() -> chatArea.appendText(finalGameMessage + "\n"));

                        }
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> chatArea.appendText("Connessione chiusa dal ServerGame.\n"));
                }
            }).start();
        } catch (Exception e) {
            Platform.runLater(() -> chatArea.appendText("Errore di connessione al ServerGame.\n"));
        }
    }


    @Override
    public void stop() throws Exception {
        if (socket != null) socket.close();
        if (serverOut != null) serverOut.close();
        if (serverIn != null) serverIn.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
