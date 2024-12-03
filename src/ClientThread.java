import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;

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
                    out.println("Messaggio ricevuto: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                notifyAllClients("La partita è iniziata! Preparati.");
                startGame();
            }
        }
    }
    private void startGame() {
        // Logica iniziale della partita (es. invio messaggio iniziale)
        notifyAllClients("È il turno del primo giocatore!");
        Server.startTurn();
    }
    public static void startTurn() {
        synchronized (ClientThread.clientsList) {
            if (ClientThread.clientsList.isEmpty()) {
                System.out.println("Nessun client connesso. Partita terminata.");
                return;
            }

            ClientThread currentPlayer = ClientThread.clientsList.get(0); // Turno del primo giocatore
            System.out.println("È il turno di " + currentPlayer.getName() + ".");

            // Invia il messaggio al giocatore corrente
            currentPlayer.sendMessage("È il tuo turno! Lancia i dadi scrivendo 'lancia'.");
        }
    }
    public void sendMessage(String message) {
        out.println(message);
    }

}
