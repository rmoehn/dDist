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
    private enum ServerState {
        Normal, WaitForEndOfEventses, WaitForNewServerOk, InitAfterSwitch };

    private ServerState _state;
    private final BlockingQueue<Event> _serverInQueue;
    private final Map<Integer, ClientHandle> _clients;
    private int _nextID;
    private String _currentText;
    private final int _oldClientCount;
    private int _adoptedClientCount;
    private int _finishedClientsCount;

    public ServerEventDistributor(String initialText, int oldClientCount) {
        _serverInQueue  = new LinkedBlockingQueue<Event>();
        _clients        = new HashMap<>();
        _nextID         = 0;
        _currentText    = initialText;
        _oldClientCount = oldClientCount;
        _state          = ServerState.InitAfterSwitch;
    }

    public ServerEventDistributor() {
        this("", 0);
        _state = ServerState.Normal;
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

                switch (_state) {
                    case Normal:
                        handle.send(new TextInsertEvent(0, _currentText));
                        break;

                    case InitAfterSwitch:
                        ++_adoptedClientCount;

                        if (_adoptedClientCount == _oldClientCount) {
                            _adoptedClientCount = 0;
                            _state = ServerState.Normal;
                            broadcastToClients( new StartSendingEvent() );
                        }

                        break;
                    default:
                        throw new AssertionError();
                }
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
            	int senderId = ((IdEvent) event).getSenderId();
                _clients.get(senderId).disconnect((DisconnectEvent) event);
                _clients.remove(senderId);
                //break;
            }
            // Server should shut down
            else if (event instanceof ShutdownEvent) {
                // Change state
                assert(_state == ServerState.Normal);
                _state = ServerState.WaitForEndOfEventses;

                // Make clients stop sending events
                broadcastToClients( new StopSendingEvent() );
            }
            // Clients says I gave thee all, I can no more
            else if (event instanceof EndOfEventsEvent) {
                // Count up
                assert(_state == ServerState.WaitForEndOfEventses);
                ++_finishedClientsCount;

                // If every client is finished
                if (_finishedClientsCount == _clients.size()) {
                    _finishedClientsCount = 0;

                    // Notify about common state
                    broadcastToClients( new CleanStateEvent() );

                    // Assign duty of being server to one of them
                    ClientHandle designatedServer
                        = _clients.values().iterator().next();
                    designatedServer.sendEvent(
                        new BecomeServerEvent(_clients.size(), _currentText));

                    // Change state
                    _state = ServerState.WaitForNewServerOk;
                }
            }
            // New server is ready
            else if (event instanceof NewServerOkEvent) {
                assert(_state == ServerState.WaitForNewServerOk);
                NewServerOkEvent nsoe = (NewServerOkEvent) event;

                broadcastToClients(
                    new ConnectToServerEvent(
                        nsoe.getListenAddress(),
                        nsoe.getListenPort()
                    )
                );

                // TODO: Close sockets properly
                break;
            }
            else {
                throw new IllegalArgumentException("Got unknown event.");
            }
        }
    }

    public void addClient(Socket socket) {
        _serverInQueue.add(new ConnectEvent(socket));
    }

    public void stop() {
        _serverInQueue.add(new ShutdownEvent());
    }

    private void broadcastToClients(Event event) {
        for (ClientHandle client : _clients.values()) {
            client.sendEvent(event);
        }
    }
}
