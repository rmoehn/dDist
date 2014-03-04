package ddist;

import java.net.Socket;

/**
 * Event indicating that someone wants to connect.
 */
public class ConnectEvent implements Event {
    private static final long serialVersionUID = 641812429761452324L;

    private final Socket _socket;

    public ConnectEvent(Socket socket) {
        _socket = socket;
    }

    public Socket getSocket() {
        return _socket;
    }
}
