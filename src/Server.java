import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final CopyOnWriteArrayList<ClientConnection> connections;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private final int gameStartDelay = 10000; // 30 secs
    private boolean waitingForPlayersAfterTriggering;
    private Serve serve;
    private final int threadNumber = 15;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Invalid usage. <port>");
        }

        new Server(Integer.parseInt(args[0]));

    }

    public Server(int port) {

        System.out.println("Server is created! Waiting for players...");

        connections = new CopyOnWriteArrayList<>();

        this.threadPool = Executors.newFixedThreadPool(threadNumber);

        try {

            setupServer(port);

        } catch (IOException e) {
            System.out.println("Port not allowed!");
        }
    }

    public boolean isWaitingForPlayersAfterTriggering() {
        return waitingForPlayersAfterTriggering;
    }

    private void setupServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        threadPool.submit(new ListenNewConnections());
        this.serve = new Serve();
        threadPool.submit(serve);
    }

    synchronized public void matchPreparation() {

        whileLoop:
        while (true) {

            for (ClientConnection clientConnection : connections) {

                if (clientConnection.isReady() && !clientConnection.isPlaying() && !isWaitingForPlayersAfterTriggering()) {

                    waitingForPlayersAfterTriggering = true;
                    Timer timer = new Timer();
                    timer.schedule(new Start(), gameStartDelay);

                    try {

                        Thread.sleep(gameStartDelay + 5000);
                        waitingForPlayersAfterTriggering = false;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue whileLoop;

                }
            }
        }
    }

    public LinkedList<ClientConnection> createPlayerList() {

        LinkedList<ClientConnection> playerList = new LinkedList<>();

        for (ClientConnection clientConnection : connections) {

            if (clientConnection.isReady() && !(clientConnection.isPlaying())) {

                playerList.add(clientConnection);
                clientConnection.setPlaying(true);

            }

        }
        return playerList;
    }

    private class Start extends TimerTask {


        @Override
        public void run() {

            waitingForPlayersAfterTriggering = false;


            LinkedList<ClientConnection> playerList = createPlayerList();


            new Game(playerList);

            for (ClientConnection clientConnection : playerList) {

                clientConnection.setReady(false);
                clientConnection.askIfReady();

            }
            playerList.clear();

        }


    }

    private class ListenNewConnections implements Runnable {

        @Override
        public void run() {

            while (true) {

                try {
                    Socket clientSocket;
                    clientSocket = serverSocket.accept();
                    System.out.println("New connection accepted!");

                    ClientConnection clientConnection = new ClientConnection(clientSocket);

                    threadPool.submit(clientConnection.getSetupTask());

                    connections.add(clientConnection);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class Serve implements Runnable {


        @Override
        public void run() {
            synchronized (serve) {
                while (true) {

                    if (isWaitingForPlayersAfterTriggering()) {
                        try {
                            serve.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    matchPreparation();

                    serve.notifyAll();

                    System.out.println("Match is preparing...");
                }
            }
        }

    }
}
