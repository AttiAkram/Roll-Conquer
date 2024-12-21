package com.example.rollconquer;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Game {
    private static final int BOARD_SIZE = 100;
    private static final Cell[] board = new Cell[BOARD_SIZE];
    private static final Random random = new Random();
    private static int turnCounter = 0;

    // Inizializza il tabellone
    private static void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE - 1; i++) {
            int probability = random.nextInt(100) + 1;
            if (probability <= 40) {
                board[i] = new Cell(ZoneType.NEUTRAL, "Zona Neutra");
            } else if (probability <= 60) {
                board[i] = new Cell(ZoneType.REST, "Zona Riposo");
            } else if (probability <= 80) {
                board[i] = new Cell(ZoneType.HOSTILE, "Zona Ostile");
            } else {
                board[i] = new Cell(ZoneType.TREASURE, "Zona Tesoro");
            }
        }
        board[BOARD_SIZE - 1] = new Cell(ZoneType.FINAL, "Zona Finale");
    }

    // Lancia un dado con numero di facce specificato
    static int rollDice(int sides) {
        return random.nextInt(sides) + 1;
    }

    // Controlla se la partita è terminata
    private static boolean checkGameEnd(ArrayList<Player> players) {
        for (Player player : players) {
            if (player.getPosition() >= BOARD_SIZE - 1) {
                System.out.println(player.getName() + " è arrivato alla zona finale!");
                return true;
            }
        }
        return false;
    }
}

// Classe Giocatore
class Player {
    private String name;
    private int position;
    private int totalScore;
    private int currentBonus;
    private int currentMalus;
    private String lastZone;
    public final ArrayList<Integer> historyThrow = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.totalScore = 0;
        this.currentBonus = 0;
        this.currentMalus = 0;
    }

    public String getName() {
        return name;
    }

    public String getLastZone() {
        return lastZone;
    }

    public int getPosition() {
        return position;
    }

    public int calculateMovement(int diceSum) {
        totalScore = diceSum + currentBonus + currentMalus;
        return totalScore;
    }

    public void move(int movement, Cell[] board) {
        position += movement;
        if (position >= board.length - 1) {
            position = board.length - 1;
            System.out.println(name + " è arrivato alla zona finale!");
            return;
        }

        Cell currentCell = board[position];
        System.out.println(name + " è atterrato in: " + currentCell.name);

        switch (currentCell.zoneType) {
            case TREASURE:
                int treasureBonus = rollTreasureBonus();
                currentBonus += treasureBonus;
                System.out.println("Hai trovato un tesoro! Bonus aggiunto: " + treasureBonus);
                lastZone = "Hai trovato un tesoro!";
                break;

            case HOSTILE:
                int hostilePenalty = rollHostilePenalty();
                currentMalus += hostilePenalty;
                System.out.println("Zona Ostile! Penalità subita: " + hostilePenalty);
                lastZone = "Zona Ostile!";
                break;

            case REST:
                resetBonusMalus();
                lastZone = "Zona Riposo!";
                break;

            case NEUTRAL:
                System.out.println("Zona Neutra: Nessun effetto.");
                lastZone = "Zona Neutra.";

                break;

            default:
                break;
        }
    }

    private void resetBonusMalus() {
        currentBonus = 0;
        currentMalus = 0;
        System.out.println("Bonus e malus sono stati azzerati.");
    }

    private static int rollTreasureBonus() {
        int roll = Game.rollDice(20);
        if (roll <= 10) return 1;
        else if (roll <= 16) return 2;
        else if (roll <= 19) return 3;
        else return 5;
    }

    private static int rollHostilePenalty() {
        int roll = Game.rollDice(20);
        if (roll <= 15) return -1;
        else if (roll <= 19) return -3;
        else return -5;
    }

    public String showInfo() {
        return "----- Informazioni di " + name + " -----\n " + "Posizione: " + position + "\n" + "Bonus Attuale: " + currentBonus + "\n" + "Malus Attuale: " + currentMalus + "\n" + "Ultimo Movimento: " + totalScore + "\n" + "----------------------------------------" + "\n";
        /*System.out.println("----- Informazioni di " + name + " -----");
        System.out.println("Posizione: " + position);
        System.out.println("Bonus Attuale: " + currentBonus);
        System.out.println("Malus Attuale: " + currentMalus);
        System.out.println("Ultimo Movimento: " + totalScore);
        System.out.println("----------------------------------------");*/
    }

}

// Enum e Classe Cella per le costanti
enum ZoneType {
    NEUTRAL, REST, HOSTILE, TREASURE, FINAL
}

class Cell {
    ZoneType zoneType;
    String name;

    public Cell(ZoneType zoneType, String name) {
        this.zoneType = zoneType;
        this.name = name;
    }
}
/*    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Player> players = new ArrayList<>();

        // Creazione dei giocatori
        System.out.println("Quanti giocatori partecipano?");
        int playerCount = Integer.parseInt(scanner.nextLine());
        for (int i = 1; i <= playerCount; i++) {
            players.add(new Player("Giocatore " + i));
        }

        initializeBoard();

        // Gioco principale
        while (!checkGameEnd(players)) {
            turnCounter++;
            System.out.println("\n--- Turno " + turnCounter + " ---");

            for (Player player : players) {
                System.out.println("\nÈ il turno di " + player.getName());

                boolean validChoice = false;
                while (!validChoice) {
                    System.out.println("Scegli un'opzione:");
                    System.out.println("1. Lancia i dadi");
                    System.out.println("2. Mostra le tue informazioni");
                    System.out.println("3. Esci dal gioco");

                    String input = scanner.nextLine();

                    switch (input) {
                        case "1":
                            validChoice = true;

                            // Lancio dei dadi
                            int dice1 = rollDice(6);
                            int dice2 = rollDice(6);
                            int diceSum = dice1 + dice2;

                            System.out.println("Hai lanciato: " + dice1 + " + " + dice2 + " = " + diceSum);

                            // Movimento del giocatore
                            int movement = player.calculateMovement(diceSum);
                            player.move(movement, board);

                            break;

                        case "2":
                            player.showInfo();
                            break;

                        case "3":
                            System.out.println("Grazie per aver giocato. Arrivederci!");
                            System.exit(0);
                            break;

                        default:
                            System.out.println("Input non valido. Riprova.");
                            break;
                    }
                }
            }
        }

        System.out.println("La partita è finita! Congratulazioni ai vincitori!");
        scanner.close();
    }*/