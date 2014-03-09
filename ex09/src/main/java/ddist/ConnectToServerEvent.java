package ddist;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Event indicating that a client should connect to a new server.
 */
public class ConnectToServerEvent implements Event {
    private static final long serialVersionUID = 7931934699415779535L;
    
    private final InetAddress _serverAddress;
    private final int _serverPort;
    private Socket _socket;

    public ConnectToServerEvent(InetAddress serverAddress, int serverPort) {
        _serverAddress = serverAddress;
        _serverPort    = serverPort;
    }

    /**
     * (Only to be used at the client.) Return a socket connected to the
     * server.
     */
    public Socket getSocket() {
        if (_socket == null) {
            try {
                _socket = new Socket(_serverAddress, _serverPort);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return _socket;
    }
}
