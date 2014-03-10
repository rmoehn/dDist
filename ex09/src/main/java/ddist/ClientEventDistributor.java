package ddist;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Central mechanism for optimistic synchronisation. Described in
 * Nichols, Curtis, Dixon, Lamping: High-Latency, Low-Bandwidth Windowing in
 * the Jupiter Collaboration System. UIST 95 Pittsburgh PA USA, Proceedings.
 */
public class ClientEventDistributor implements Runnable {
    private enum ClientState {
        Normal, StopSending, WaitForNewServer, WaitForSending };
    private Jupiter _jupiter = new Jupiter(false);

    private ClientState _state;

    private Client _containingClient;
    private Socket _socket;
    private final Callbacks _callbacks;

    private BlockingQueue<Event> _inQueue;
    private BlockingQueue<Event> _outQueue;
    private BlockingQueue<Event> _toDisplayer;

    private BlockingQueue<Event> _newServerBuffer;

    public ClientEventDistributor(Client containingClient,
            BlockingQueue<Event> inQueue, BlockingQueue<Event> outQueue,
            BlockingQueue<Event> toDisplayer, Callbacks callbacks) {
        _containingClient = containingClient;
        _inQueue          = inQueue;
        _outQueue         = outQueue;
        _toDisplayer      = toDisplayer;
        _state            = ClientState.Normal;
        _callbacks        = callbacks;
    }

    public void run() {
        while (true) {
            Event event = null;
            try {
                event = _inQueue.take();
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
            System.out.println("Client: " + event);

            // Generate(op)
            if (event instanceof TextChangeEvent) {
                // Buffer events if server transition in progress
                if (_state != ClientState.Normal) {
                    _newServerBuffer.add(event);
                    continue;
                }

                // apply op locally
                TextChangeEvent localOp = (TextChangeEvent) event;
                _toDisplayer.add(localOp);

                // send(op, my Msgs, otherMsgs)
                JupiterEvent jupiterEvent = _jupiter.generate(localOp);
                _outQueue.add(jupiterEvent);
            }
            // Receive(msg)
            else if (event instanceof JupiterEvent) {
                // apply msg.op locally
                JupiterEvent received = (JupiterEvent) event;
                _toDisplayer.add(
                    _jupiter.receive(received).getContainedEvent() );
            }
            // Want to disconnect
            else if (event instanceof DisconnectEvent) {
                // When it comes back, we can connect to the new server
                if (_state == ClientState.WaitForSending) {
                    _containingClient.startCommunication(_socket);

                    // Create new Jupiter
                    _jupiter = new Jupiter(false);
                    continue;
                }

                // Postpone disconnecting if elsewhere in server change process
                if (_state != ClientState.Normal) {
                    _newServerBuffer.add(event);
                    continue;
                }

                // Pass event on and stop work
                _toDisplayer.add(event);
                _callbacks.disconnectedFromServer();
                break;
            }
            // Server goes away
            else if (event instanceof StopSendingEvent) {
                // Change state and create new buffer
                assert(_state == ClientState.Normal);
                _state           = ClientState.StopSending;
                _newServerBuffer = new LinkedBlockingQueue<>();

                // Indicate that we won't be sending any more
                _outQueue.add( new EndOfEventsEvent() );
            }
            // Server says it has no more to send
            else if (event instanceof CleanStateEvent) {
                // Change state
                assert(_state == ClientState.StopSending);
                _state = ClientState.WaitForNewServer;
            }
            // If we should become new server
            else if (event instanceof BecomeServerEvent) {
                assert(_state == ClientState.WaitForNewServer);
                BecomeServerEvent bse = (BecomeServerEvent) event;
                _containingClient.setIsRunningServer();
                // TODO: stop forwarder

                // Start up server
                Server server = new Server(
                                    _containingClient.getListenPort(),
                                    bse.getText(),
                                    bse.getOldClientCount(),
                                    _callbacks
                                );
                _containingClient.setServer(server);
                server.start();

                // Send NewServerOkEvent to old server
                _outQueue.add(
                    new NewServerOkEvent(
                        server.getListenAddress(),
                        server.getListenPort()
                    )
                );
            }
            // Old server says the new server is in place
            else if (event instanceof ConnectToServerEvent) {
                // Change state
                assert(_state == ClientState.WaitForNewServer);
                _state = ClientState.WaitForSending;
                ConnectToServerEvent ctse = (ConnectToServerEvent) event;

                // Make the EventSender switch servers, too
                _outQueue.add( new DisconnectEvent() );

                // Store information to where to connect after disconnecting
                _socket = ctse.getSocket();
            }
            // New server says we can send events again
            else if (event instanceof StartSendingEvent) {
                // Change state
                assert(_state == ClientState.WaitForSending);
                _state = ClientState.Normal;

                // Put events from buffer into inqueue
                for (Event bufEv : _newServerBuffer) {
                    _inQueue.add(bufEv);
                }
                _newServerBuffer = null;
            }
            else {
                throw new IllegalArgumentException("Got unknown event.");
            }
        }
    }

    public void sendDisconnect() {
        _outQueue.add(new DisconnectEvent());
        return;
    }
}
