import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Player {
    private String playerId;
    private long balance;
    private int totalBets;
    private int wonBets;

    public Player(String playerId) {
        this.playerId = playerId;
        this.balance = 0;
        this.totalBets = 0;
        this.wonBets = 0;
    }

    public String getPlayerId() {
        return playerId;
    }

    public long getBalance() {
        return balance;
    }

    public void deposit(long amount) {
        balance += amount;
    }

    public boolean withdraw(long amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        } else {
            return false;
        }
    }

    public void placeBet(double amount, boolean won) {
        totalBets++;
        if (won) {
            wonBets++;
            balance += (long) amount;
        } else {
            balance -= (long) amount;
        }
    }

    public BigDecimal getWinRate() {
        if (totalBets == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wonBets).divide(new BigDecimal(totalBets), 2, BigDecimal.ROUND_HALF_UP);
    }
}

class Match {
    private String matchId;
    private double rateA;
    private double rateB;
    private String result;

    public Match(String matchId, double rateA, double rateB, String result) {
        this.matchId = matchId;
        this.rateA = rateA;
        this.rateB = rateB;
        this.result = result;
    }

    public String getMatchId() {
        return matchId;
    }

    public double getRate(String side) {
        return (side.equals("A")) ? rateA : rateB;
    }

    public String getResult() {
        return result;
    }
}

class BettingSystem {
    private Map<String, Player> players;
    private Map<String, Match> matches;
    private long casinoBalance;

    public BettingSystem() {
        this.players = new HashMap<>();
        this.matches = new HashMap<>();
        this.casinoBalance = 0;
    }

    public void addPlayer(String playerId) {
        players.put(playerId, new Player(playerId));
    }

    public void addMatch(String matchId, double rateA, double rateB, String result) {
        matches.put(matchId, new Match(matchId, rateA, rateB, result));
    }

    public void processBet(String playerId, String matchId, String side, double betSize) {
        Player player = players.get(playerId);
        Match match = matches.get(matchId);

        if (player == null || match == null) {
            System.out.println("Invalid player or match ID.");
            return;
        }

        if (betSize > player.getBalance()) {
            System.out.println("Illegal operation: Insufficient balance for the bet.");
            return;
        }

        double rate = match.getRate(side);

        if (match.getResult().equals(side)) {
            player.placeBet(betSize, true);
        } else {
            player.placeBet(betSize, false);
        }

        // Update casino balance only for legitimate bets
        casinoBalance += (match.getResult().equals(side)) ? betSize * (rate - 1) : 0;
    }

    public void printResults() {
        try (FileWriter writer = new FileWriter("results.txt")) {
            // List of legitimate players
            writer.write("Legitimate Players:\n");
            for (Player player : players.values()) {
                writer.write(player.getPlayerId() + " " + player.getBalance() + " " + player.getWinRate() + "\n");
            }
            writer.write("\n");

            // List of illegitimate players
            writer.write("Illegitimate Players:\n");
            for (Player player : players.values()) {
                if (player.getBalance() < 0) {
                    writer.write(player.getPlayerId() + " BET null null null " + player.getBalance() + "\n");
                    break;  // Only print the first illegal operation
                }
            }
            writer.write("\n");

            // Casino balance changes
            writer.write("Casino Balance Change:\n");
            writer.write(casinoBalance + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter method for accessing players map
    public Map<String, Player> getPlayers() {
        return players;
    }
}

public class BettingSimulation {
    public static void main(String[] args) {
        BettingSystem bettingSystem = new BettingSystem();

        // Read player data from file
        readPlayerData(bettingSystem, "player_data.txt");

        // Read match data from file
        readMatchData(bettingSystem, "match_data.txt");

        // Process bets
        processBets(bettingSystem, "player_data.txt");

        // Print results and write to results.txt
        bettingSystem.printResults();
    }

    private static void readPlayerData(BettingSystem bettingSystem, String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                String playerId = data[0];
                bettingSystem.addPlayer(playerId);

                // Check the operation type and perform the corresponding action
                String operation = data[1];
                if (operation.equals("DEPOSIT")) {
                    long depositAmount = Long.parseLong(data[3]);
                    bettingSystem.getPlayers().get(playerId).deposit(depositAmount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readMatchData(BettingSystem bettingSystem, String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                String matchId = data[0];
                double rateA = Double.parseDouble(data[1]);
                double rateB = Double.parseDouble(data[2]);
                String result = data[3];
                bettingSystem.addMatch(matchId, rateA, rateB, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processBets(BettingSystem bettingSystem, String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                String playerId = data[0];
                String operation = data[1];

                // Check the operation type and perform the corresponding action
                if (operation.equals("BET")) {
                    String matchId = data[2];
                    double betSize = Double.parseDouble(data[3]);
                    String side = data[4];
                    bettingSystem.processBet(playerId, matchId, side, betSize);
                } else if (operation.equals("WITHDRAW")) {
                    long withdrawAmount = Long.parseLong(data[3]);
                    bettingSystem.getPlayers().get(playerId).withdraw(withdrawAmount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
