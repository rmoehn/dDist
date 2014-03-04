package ddist;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Central mechanism for optimistic synchronisation. Described in
 * Nichols, Curtis, Dixon, Lamping: High-Latency, Low-Bandwidth Windowing in
 * the Jupiter Collaboration System. UIST 95 Pittsburgh PA USA, Proceedings.
 */
public class ServerEventDistributor implements Runnable {


    private BlockingQueue<Event> _serverInQueue;
    private Map<Integer,ClientHandle> _clients;
    private int _nextID;

    public ServerEventDistributor(BlockingQueue<Event> toLocalClient) {
        _serverInQueue = new LinkedBlockingQueue<Event>();
        _clients = new HashMap<>();
        _clients.put(0, new ClientHandle(_serverInQueue, toLocalClient));
        _nextID = 1;
    }

    public void run() {
        while (true) {
            Event event = null;
            try {
                event = _serverInQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

            if (event instanceof JupiterEvent) {
                JupiterEvent received = (JupiterEvent) event;
                ClientHandle sender = _clients.get(received.getSenderID());

                JupiterEvent transformed = sender.receive(received);

                for (ClientHandle client : _clients.values()) {
                    if (client == sender) {
                        continue;
                    }
                    client.send((TextChangeEvent) transformed.getContainedEvent());
                }
            }
            // Want to connect
            else if (event instanceof ConnectEvent) {
                ClientHandle handle = new ClientHandle(_serverInQueue,
                                                       ((ConnectEvent) event).getSocket());
                _clients.put(_nextID,handle);
                _nextID++;
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
                // Pass event on and stop work
                // _toDisplayer.add(event);
                break;
            }
            else {
                throw new IllegalArgumentException("Got unknown event.");
            }
        }
    }
}
