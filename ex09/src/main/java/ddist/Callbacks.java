package ddist;

import java.net.InetAddress;

public interface Callbacks {
    public void serverShutDown();
    public void clientConnected(int clientCnt);
    public void clientDisconnected(int clientCnt);
    public void connectedToServer(InetAddress address, int port);
    public void disconnectedFromServer();
}
