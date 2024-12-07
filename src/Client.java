import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Indirizzo del server
        int port = 12345; // Porta del server

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connesso al server!"+socket);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(in.readLine()); // Messaggio di benvenuto
0
            String userInput;
            System.out.println("Scrivi un comando ('lancia' per lanciare i dadi, 'exit' per uscire):");
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput); // Invia messaggio al server
                String serverResponse = in.readLine();
                System.out.println("Risposta dal server: " + serverResponse);

                if (userInput.equalsIgnoreCase("exit") || serverResponse.equalsIgnoreCase("null")) {
                    System.out.println("Disconnessione...");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Disconnesso.");
            System.exit(0);
        }
    }
}
