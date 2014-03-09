package ddist;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandle {
    private BlockingQueue<Event> _serverInQueue;
    private BlockingQueue<Event> _outQueue;

    private Jupiter _jupiter;
    private final int _clientId;

    private Thread _receiverThread;
    private Thread _senderThread;

    private boolean _isRunningServer;

    public ClientHandle(BlockingQueue<Event> serverInQueue, Socket socket, int clientId) {
        _serverInQueue = serverInQueue;
        _clientId      = clientId;
        _jupiter       = new Jupiter(true);
        _outQueue      = new LinkedBlockingQueue<Event>();

        // Start thread for adding incoming events to the inqueue
        EventReceiver rec
            = new EventReceiver(socket, _serverInQueue, _outQueue, _clientId);
        _receiverThread = new Thread(rec);
        _receiverThread.start();

        // Start thread for taking outgoing events from the outqueue
        EventSender sender
            = new EventSender(socket, _outQueue);
        _senderThread = new Thread(sender);
        _senderThread.start();
    }

    public void send(TextChangeEvent event) {
        sendEvent(_jupiter.generate(event));
    }

    public void sendEvent(Event event) {
        _outQueue.add(event);
    }

    public JupiterEvent receive(JupiterEvent event) {
        return _jupiter.receive(event);
    }

    public void setIsRunningServer() {
        _isRunningServer = true;
    }

    public boolean isRunningServer() {
        return _isRunningServer;
    }

    public void disconnect(DisconnectEvent event) {
        _outQueue.add(event);
        try {
			_receiverThread.join();
	        _senderThread.join();
		} catch (InterruptedException e) {
			throw new AssertionError();
		}
        return;
    }
}
