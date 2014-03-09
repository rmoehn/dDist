package ddist;

import java.net.InetAddress;

/**
 * Event indicating that the new server has started up and is ready to adopt
 * the old clients.
 */
public class NewServerOkEvent implements Event {
    private static final long serialVersionUID = -1280460253149446655L;
    private InetAddress _listenAddress;
    private int _listenPort;

    public NewServerOkEvent(InetAddress listenAddress, int listenPort) {
        _listenAddress = listenAddress;
        _listenPort    = listenPort;
    }

    public InetAddress getListenAddress() {
        return _listenAddress;
    }

    public int getListenPort() {
        return _listenPort;
    }    
}
