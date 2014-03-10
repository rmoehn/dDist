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
        Normal, WaitForEndOfEventses, WaitForNewServerOk, InitAfterSwitch,
        WaitForClientsDisconnect };

    private ServerState _state;
    private final BlockingQueue<Event> _serverInQueue;
    private final Map<Integer, ClientHandle> _clients;
    private int _nextID;
    private String _currentText;
    private final int _oldClientCount;
    private int _adoptedClientCount;
    private int _finishedClientsCount;
    private int _remainingClientsCount;
    private final Callbacks _callbacks;
    private final Server _containingServer;

    public ServerEventDistributor(String initialText, int oldClientCount,
            Callbacks callbacks, Server containingServer) {
        _serverInQueue    = new LinkedBlockingQueue<Event>();
        _clients          = new HashMap<>();
        _nextID           = 0;
        _currentText      = initialText;
        _oldClientCount   = oldClientCount;
        _state            = ServerState.InitAfterSwitch;
        _callbacks        = callbacks;
        _containingServer = containingServer;
    }

    public ServerEventDistributor(Callbacks callbacks,
            Server containingServer) {
        this("", 0, callbacks, containingServer);
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
                _callbacks.clientConnected( _clients.size() );

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
            // Clients says it runs on the same process as server
            else if (event instanceof ImLocalClientEvent) {
                // TODO: Think of whether this is safe.
                ImLocalClientEvent ilce = (ImLocalClientEvent) event;
                _clients.get( ilce.getSenderId() ).setIsRunningServer();
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
            	int senderId = ((IdEvent) event).getSenderId();
                ClientHandle client = _clients.get(senderId);
                client.disconnect((DisconnectEvent) event);

                // If the local client has disconnected
                if (client.isRunningServer()) {
                    // Initiate stopping in the next iteration
                    this.stop();
                }

                _clients.remove(senderId);
                _callbacks.clientDisconnected( _clients.size() );

                // Make sure all clients are gone before dying
                if (_state == ServerState.WaitForClientsDisconnect) {
                    --_remainingClientsCount;

                    if (_remainingClientsCount == 0) {
                        _callbacks.serverShutDown();
                        break;
                    }
                }
            }
            // Server should shut down
            else if (event instanceof ShutdownEvent) {
                // Change state
                assert(_state == ServerState.Normal);
                _state = ServerState.WaitForEndOfEventses;

                // Make the clients stop sending events
                broadcastToClients( new StopSendingEvent() );

                // Make thread listening for new connections stop
                _containingServer.stopConnectionListener();
                    // Might take more than Server.ACCEPT_TIMEOUT milliseconds
                    //
                // Shut down now if the local client left as the last one
                if (_clients.size() == 0) {
                    _callbacks.serverShutDown();
                    break;
                }
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
                    ClientHandle designatedServer = null;
                    for (ClientHandle client : _clients.values()) {
                        if (! client.isRunningServer()) {
                            designatedServer = client;
                            break;
                        }
                    }
                    if (designatedServer == null) {
                        throw new IllegalStateException(
                            "Lacking client to move server to.");
                    }
                    designatedServer.sendEvent(
                        new BecomeServerEvent(
                            _clients.size(),
                            _currentText
                        )
                    );

                    // Change state
                    _state = ServerState.WaitForNewServerOk;
                }
            }
            // New server is ready
            else if (event instanceof NewServerOkEvent) {
                // Change state and prepare for counting client disconnects
                assert(_state == ServerState.WaitForNewServerOk);
                _state = ServerState.WaitForClientsDisconnect;
                _remainingClientsCount = _clients.size();

                NewServerOkEvent nsoe = (NewServerOkEvent) event;

                broadcastToClients(
                    new ConnectToServerEvent(
                        nsoe.getListenAddress(),
                        nsoe.getListenPort()
                    )
                );
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
