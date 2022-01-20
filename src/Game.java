import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {

    private final PhraseGenerator phraseGenerator;
    private ExecutorService threadPool;
    private final LinkedList<ClientConnection> playerList;
    private final int roundNumber = 10;
    private String currentRoundCorrectPhrase;
    private final int threadNumber = 15;
    private int maxTries;

    public Game(LinkedList<ClientConnection> playerList) {

        phraseGenerator = new PhraseGenerator();

        threadPool = Executors.newFixedThreadPool(threadNumber);

        this.playerList = playerList;

        this.maxTries = playerList.size();

        setPlayingStatus(true);

        start();

    }

    public StringBuilder getScoreBoard() {

        StringBuilder string = new StringBuilder();
        string.append("*********************************************************************\n");
        for (ClientConnection player : playerList) {

            string.append(player.getName());
            string.append(": ");
            string.append(player.getScore());
            string.append("\n");

        }
        string.append("*********************************************************************\n");
        return string;

    }

    private String gameOverBanner() {
        return "\n" +
                " ██████   █████  ███    ███ ███████      ██████  ██    ██ ███████ ██████  \n" +
                "██       ██   ██ ████  ████ ██          ██    ██ ██    ██ ██      ██   ██ \n" +
                "██   ███ ███████ ██ ████ ██ █████       ██    ██ ██    ██ █████   ██████  \n" +
                "██    ██ ██   ██ ██  ██  ██ ██          ██    ██  ██  ██  ██      ██   ██ \n" +
                " ██████  ██   ██ ██      ██ ███████      ██████    ████   ███████ ██   ██ \n" +
                "                                                                          \n" +
                "                                                                          \n";
    }

    private void setPlayingStatus(boolean playingStatus) {
        for (ClientConnection player : playerList) {
            player.setPlaying(playingStatus);
            player.setReady(false);
        }
    }

    synchronized public void start() {

        int currentRound = 0;

        while (currentRound < roundNumber) {

            System.out.println("New round started");

            setupPhrase();

            for (ClientConnection player : playerList) {

                threadPool.submit(new playerListener(player));

            }

            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resetThreadPool();
            currentRound++;
        }

        sendMessageToPlayers(gameOverBanner());
        generateLeaderboard();
        setPlayingStatus(false);

    }

    private void generateLeaderboard() {
        sendMessageToPlayers("\n" + "***************************** FINAL SCORE *****************************\n" + getScoreBoard().toString());

        ClientConnection finalWinner = playerList.getFirst();

        for (ClientConnection player : playerList) {
            if (finalWinner.getScore() < player.getScore()) {
                finalWinner = player;
            }
        }

        sendMessageToPlayers(finalWinner.getName().concat(", you are the winner!"));
    }

    private void resetThreadPool() {
        threadPool.shutdown();
        threadPool = Executors.newFixedThreadPool(threadNumber);
    }

    private void setupPhrase() {
        String[] phrases = phraseGenerator.grabTargetPhrases(false);
        String rightPhrase = phrases[0];
        String shuffledPhrase = phrases[1];

        currentRoundCorrectPhrase = rightPhrase;

        sendMessageToPlayers("\n" + shuffledPhrase);
    }

    public synchronized void checkPhraseCorrect(ClientConnection player, String answer) {

        if (answer.equalsIgnoreCase(currentRoundCorrectPhrase)) {
            player.updateScore(10);
            endRound(player, true);
            return;
        }

        player.updateScore(-10);
        endRound(player, false);


    }

    public void sendMessageToPlayers(String message) {

        for (ClientConnection player : playerList) {

            player.getOutput().println(message);
            player.getOutput().flush();

        }

    }

    synchronized public void endRound(ClientConnection winner, boolean won) {

        if (won) {
            sendMessageToPlayers("\n" + winner.getName().concat(" won this round and got ten points!\n"));
            sendMessageToPlayers("\nThe correct phrase was:\n"+currentRoundCorrectPhrase+"\n");
            resetRound();
            return;
        } else {
            sendMessageToPlayers("\n" + winner.getName().concat(" gave an incorrect answer.\n").concat(winner.getName().concat(" lost ten points.\n")));
            maxTries--;
        }
        if (maxTries == 0) {
            sendMessageToPlayers("\nThe correct phrase was:\n"+currentRoundCorrectPhrase+"\n");
            resetRound();
        }



    }

    private void resetRound() {
        notifyAll();
        maxTries = playerList.size();
        sendMessageToPlayers(getScoreBoard().toString());
    }

    private class playerListener implements Runnable {

        private final ClientConnection clientConnection;

        public playerListener(ClientConnection clientConnection) {

            this.clientConnection = clientConnection;
        }

        @Override
        public void run() {

            try {

                String messageReceived = clientConnection.getInput().readLine();

                checkPhraseCorrect(clientConnection, messageReceived);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


}