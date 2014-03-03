package ddist;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandle {

    private BlockingQueue<Event> _serverInQueue;
    private BlockingQueue<Event> _outQueue;

    private Jupiter _jupiter;

    public ClientHandle(BlockingQueue<Event> serverInQueue,
                        BlockingQueue<Event> outQueue) {
        _serverInQueue = serverInQueue;
        _outQueue      = outQueue;
        _jupiter       = new Jupiter(true);
    }

    public ClientHandle(BlockingQueue<Event> serverInQueue, Socket socket) {
        _serverInQueue = serverInQueue;
        _jupiter       = new Jupiter(true);
        _outQueue      = new LinkedBlockingQueue<Event>();

        // Start thread for adding incoming events to the inqueue
        EventReceiver rec
            = new EventReceiver(socket, _serverInQueue, _outQueue);
        Thread receiverThread = new Thread(rec);
        receiverThread.start();

        // Start thread for taking outgoing events from the outqueue
        EventSender sender
            = new EventSender(socket, _outQueue);
        Thread senderThread = new Thread(sender);
        senderThread.start();
    }

    public void send(TextChangeEvent event) {
        _outQueue.add(_jupiter.generate(event));
    }

    public JupiterEvent receive(JupiterEvent event) {
        return _jupiter.receive(event);
    }
}
