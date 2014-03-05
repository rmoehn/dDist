package ddist;

import java.net.Socket;
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
    private Map<Integer, ClientHandle> _clients;
    private int _nextID;
    private String _currentText;

    public ServerEventDistributor() {
        _serverInQueue = new LinkedBlockingQueue<Event>();
        _clients       = new HashMap<>();
        _nextID        = 0;
        _currentText   = "";
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
            System.out.println("Server: " + event);
            if (event instanceof JupiterEvent) {
                JupiterEvent received = (JupiterEvent) event;
                ClientHandle sender = _clients.get(received.getSenderId());

                JupiterEvent transformed = sender.receive(received);
                TextChangeEvent contained = (TextChangeEvent) transformed.getContainedEvent();

                _currentText = contained.apply(_currentText);

                for (ClientHandle client : _clients.values()) {
                    if (client == sender) {
                        continue;
                    }
                    client.send(contained);
                }
            }
            // Want to connect
            else if (event instanceof ConnectEvent) {
                ClientHandle handle = new ClientHandle(_serverInQueue,
                                                       ((ConnectEvent) event).getSocket(),
                                                       _nextID);
                _clients.put(_nextID, handle);
                _nextID++;

                handle.send(new TextInsertEvent(0, _currentText));
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
            	int senderId = ((IdEvent) event).getSenderId();
                _clients.get(senderId).disconnect((DisconnectEvent) event);
                _clients.remove(senderId);
                //break;
            }
            else {
                throw new IllegalArgumentException("Got unknown event.");
            }
        }
    }

    public void addClient(Socket socket) {
        _serverInQueue.add(new ConnectEvent(socket));
    }
}
