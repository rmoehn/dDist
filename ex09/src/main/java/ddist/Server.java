package ddist;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

public class Server {
    private final int _port;
    private InetAddress _address;
    final ServerEventDistributor _eventDistributor;
    private final Semaphore _readyForAccepting = new Semaphore(0);
    private static final int ACCEPT_TIMEOUT = 500;
    private Thread _connectionListener;

    public Server(String initialText, int oldClientsCount, Callbacks
            callbacks) {
        _port             = callbacks.getListenPort();
        _address          = callbacks.getListenAddress();
        _eventDistributor = new ServerEventDistributor(
                                initialText,
                                oldClientsCount,
                                callbacks,
                                this
                            );
    }

    public Server(Callbacks callbacks) {
        _port             = callbacks.getListenPort();
        _address          = callbacks.getListenAddress();
        _eventDistributor = new ServerEventDistributor(callbacks, this);
    }

    public void start() {
        listenForConnection();
        startEventDistributor();

        try {
            _readyForAccepting.acquire();
        }
        catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    private void listenForConnection() {
        // Asynchronously wait for a connection
        _connectionListener = new Thread( new Runnable() {
                public void run() {
                    try {
                        ServerSocket servSock
                            = new ServerSocket(
                                  _port,
                                  50,
                                  _address
                              );
                        servSock.setSoTimeout( ACCEPT_TIMEOUT  );
                        _readyForAccepting.release();

                        while (! Thread.interrupted()) {
                            // Wait for an incoming connection
                            Socket socket = null;
                            try {
                                socket = servSock.accept();
                            }
                            catch (SocketTimeoutException _) {
                                // Timout only for periodical interrupt checks
                                continue;
                            }

                            _eventDistributor.addClient(socket);
                        }

                        servSock.close();
                    }
                    catch (IOException ex) {
                        System.out.println("Cannot listen."); //TODO: GUI
                        ex.printStackTrace();
                        return;
                    }
                }
            } );
        _connectionListener.start();
    }

    public void stopConnectionListener() {
        _connectionListener.interrupt();
        try {
            _connectionListener.join();
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    public InetAddress getListenAddress() {
        return _address;
    }

    public int getListenPort() {
        return _port;
    }

    private void startEventDistributor() {
        // Start thread containing the Jupiter client/server
        Thread eventDistributorThread = new Thread(_eventDistributor);
        eventDistributorThread.start();
    }
}
