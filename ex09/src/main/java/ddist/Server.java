package ddist;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private int _port;
    ServerEventDistributor _eventDistributor;

    public Server(int port) {
        _port = port;
        _eventDistributor = new ServerEventDistributor();
    }

    public void start() {
        listenForConnection();
        startEventDistributor();
    }

    private void listenForConnection() {
        // Asynchronously wait for a connection
        new Thread( new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            @SuppressWarnings("resource")
                                ServerSocket servSock = new ServerSocket(_port);

                            // Wait for an incoming connection
                            Socket socket = null;
                            socket = servSock.accept();
                            _eventDistributor.addClient(socket);
                        }
                        catch (IOException ex) {
                            System.out.println("Cannot listen."); //TODO: GUI
                            ex.printStackTrace();
                            return;
                        }
                    }
                }
            } ).start();
    }

    private void startEventDistributor() {
        // Start thread containing the Jupiter client/server
        Thread eventDistributorThread = new Thread(_eventDistributor);
        eventDistributorThread.start();
    }
}
