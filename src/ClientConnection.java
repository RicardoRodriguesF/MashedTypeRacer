import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private final Socket clientSocket;
    private boolean isPlaying;
    private boolean isReady;
    private String name;
    private int score;
    private BufferedReader input;
    private PrintWriter output;
    private String welcomingBanner() {
        return "\n" +
                "███    ███  █████  ███████ ██   ██ ███████ ██████      ████████ ██    ██ ██████  ███████ ██████   █████   ██████ ███████ ██████  \n" +
                "████  ████ ██   ██ ██      ██   ██ ██      ██   ██        ██     ██  ██  ██   ██ ██      ██   ██ ██   ██ ██      ██      ██   ██ \n" +
                "██ ████ ██ ███████ ███████ ███████ █████   ██   ██        ██      ████   ██████  █████   ██████  ███████ ██      █████   ██████  \n" +
                "██  ██  ██ ██   ██      ██ ██   ██ ██      ██   ██        ██       ██    ██      ██      ██   ██ ██   ██ ██      ██      ██   ██ \n" +
                "██      ██ ██   ██ ███████ ██   ██ ███████ ██████         ██       ██    ██      ███████ ██   ██ ██   ██  ██████ ███████ ██   ██ \n" +
                "                                                                                                                                 \n" +
                "                                                                                                                                 \n"
                +"\nInstructions: \nThe goal is to type the correct word sequence of the phrase.\nEach correct answer is worth 10 points.\nEach incorrect answer will cost you 10 points.\nGood luck!\n";
    }

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public String getName() {
        return name;
    }

    public Setup getSetupTask() {
        return new Setup();
    }

    public BufferedReader getInput() {
        return input;
    }

    public PrintWriter getOutput() {
        return output;
    }

    public int getScore() {
        return score;
    }

    public void updateScore(int amount) {
        this.score += amount;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void setupClientConnection(Socket clientSocket) {

        try {

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        output.println(welcomingBanner());
        askName();
        askIfReady();


    }

    private void askName() {

        output.println("Please choose a nickname.");
        output.flush();

        try {

            name = input.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void askIfReady() {

        while(!isReady) {

            output.println("\nType /ready whenever you want to start.");
            output.flush();

            try {

                if (input.readLine().equals("/ready")) {
                    isReady = true;
                    output.println("\nWaiting for all the players...");
                    output.flush();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class Setup implements Runnable {
        @Override
        public void run() {
            setupClientConnection(clientSocket);
        }
    }
}