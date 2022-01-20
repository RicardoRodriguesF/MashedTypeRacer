import java.io.*;
import java.net.Socket;

public class Client {

    private final String ipAddress;
    private final int portNumber;
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    private BufferedReader keyboardInput;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid usage. <ip> <port>");
            System.exit(1);
        }

        new Client(args[0], Integer.parseInt(args[1]));

    }

    public Client(String ipAddress, int portNumber) {

        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        setUpClient();

    }

    public void setUpClient() {

        try {
            setupStreams();
            startListening();
            exportInput();

        } catch (IOException e) {
            System.out.println("I couldn't find that server. Please try again.");
            killMe();
        }
    }

    private void startListening() {
        Thread thread = new Thread(new ChatUpdater());
        thread.start();
    }

    private void setupStreams() throws IOException {
        clientSocket = new Socket(ipAddress, portNumber);

        output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        keyboardInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void killMe() {
        try {
            clientSocket.close();
            System.exit(1);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void listenServer() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            killMe();
        }
    }

    public void exportInput() {

        while (true) {
            try {

                String message = keyboardInput.readLine();
                output.println(message);
                output.flush();

            } catch (IOException e) {
                killMe();
                return;
            }
        }
    }

    private class ChatUpdater implements Runnable {


        @Override
        public void run() {

            listenServer();

        }

    }


}
