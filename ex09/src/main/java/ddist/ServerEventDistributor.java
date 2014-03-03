package ddist;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Central mechanism for optimistic synchronisation. Described in
 * Nichols, Curtis, Dixon, Lamping: High-Latency, Low-Bandwidth Windowing in
 * the Jupiter Collaboration System. UIST 95 Pittsburgh PA USA, Proceedings.
 */
public class ServerEventDistributor implements Runnable {


    private BlockingQueue<Event> _serverInQueue;
    private Map<Integer,ClientHandle> _clients;

    public ServerEventDistributor(BlockingQueue<Event> serverInQueue,
                                  BlockingQueue<Event> localClientOutQueue) {
        _serverInQueue = serverInQueue;
        _clients = new HashMap<>();
        _clients.put(0, new ClientHandle(_serverInQueue, localClientOutQueue));
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
