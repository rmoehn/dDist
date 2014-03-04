package ddist;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Central mechanism for optimistic synchronisation. Described in
 * Nichols, Curtis, Dixon, Lamping: High-Latency, Low-Bandwidth Windowing in
 * the Jupiter Collaboration System. UIST 95 Pittsburgh PA USA, Proceedings.
 */
public class ClientEventDistributor implements Runnable {

    private ClientHandle _handle;
    private BlockingQueue<Event> _inqueue;
    private BlockingQueue<Event> _toDisplayer;

    public ClientEventDistributor(BlockingQueue<Event> toLocalClient,
                                  BlockingQueue<Event> toDisplayer) {
        outQueue = null; //serverInQueue
        assert(false);
        _handle = new ClientHandle(toLocalClient, outQueue);
        _inqueue     = toLocalClient;
        _toDisplayer = toDisplayer;
    }

    public ClientEventDistributor(BlockingQueue<Event> inqueue,
                                  BlockingQueue<Event> toDisplayer,
                                  Socket socket) {
        _handle = new ClientHandle(inqueue, socket);
        _inqueue     = inqueue;
        _toDisplayer = toDisplayer;
    }

    public void run() {
        while (true) {
            Event event = null;
            try {
                event = _inqueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

            // Generate(op)
            if (event instanceof TextChangeEvent) {
                TextChangeEvent localOp = (TextChangeEvent) event;

                // apply op locally
                _toDisplayer.add(localOp);
                // send it to server
                _handle.send(localOp);
            }
            // Receive(msg)
            else if (event instanceof JupiterEvent) {
                JupiterEvent received = (JupiterEvent) event;

                // apply msg.op locally
                _toDisplayer.add(_handle.receive(received).getContainedEvent());
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
                // Pass event on and stop work
                _toDisplayer.add(event);
                break;
            }
            else {
                throw new IllegalArgumentException("Got unknown event.");
            }
        }
    }
}
