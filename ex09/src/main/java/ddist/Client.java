package ddist;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private Socket _connectSocket;
    private final int _listenPort;
    private final BlockingQueue<Event> _inQueue;
    private final BlockingQueue<Event> _toDisplayer;
    private final BlockingQueue<Event> _outQueue;
    private final ClientEventDistributor _eventDistributor;
    private boolean _isRunningServer;

    public Client(BlockingQueue<Event> inQueue, BlockingQueue<Event>
            toDisplayer, Socket connectSocket, int listenPort,
            boolean isRunningServer) {
        _listenPort       = listenPort;
        _connectSocket    = connectSocket;
        _inQueue          = inQueue;
        _toDisplayer      = toDisplayer;
        _outQueue         = new LinkedBlockingQueue<>();
        _eventDistributor = new ClientEventDistributor(
                                this,
                                _inQueue,
                                _outQueue,
                                _toDisplayer
                            );
        _isRunningServer  = isRunningServer;
    }

    public void start() {
        // Start thread containing the Jupiter client/server
        Thread eventDistributorThread = new Thread(_eventDistributor);
        eventDistributorThread.start();

        startSenderReceiver();
    }

    public void startCommunication(Socket socket) {
        _connectSocket = socket;
        startSenderReceiver();
    }

    private void startSenderReceiver() {
        // Start thread for adding incoming events to the inqueue
        EventReceiver rec
            = new EventReceiver(_connectSocket, _inQueue, _outQueue);
        Thread receiverThread = new Thread(rec);
        receiverThread.start();

        // Start thread for taking outgoing events from the outqueue
        EventSender sender = new EventSender(_connectSocket, _outQueue);
        Thread senderThread = new Thread(sender);
        senderThread.start();

        // If we're in the same process as the server, tell it
        if (_isRunningServer) {
            _outQueue.add( new ImLocalClientEvent() );
        }
    }

    public void setIsRunningServer() {
        _isRunningServer = true;
    }

    public boolean isRunningServer() {
        return _isRunningServer;
    }

    public void sendDisconnect() {
        _eventDistributor.sendDisconnect();
    }

    public BlockingQueue<Event> getInQueue() {
        return _inQueue;
    }

    public BlockingQueue<Event> getQueueToDisplayer() {
        return _toDisplayer;
    }

    public int getListenPort() {
        return _listenPort;
    }

    public void setServer(Server server) {
    }
}
