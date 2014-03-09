package ddist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Thread responsible for receiving MyTextEvents and adding them to the event
 * queue.
 */
public class EventReceiver implements Runnable {
    private Socket _socket;
    private BlockingQueue<Event> _inEventQueue;
    private BlockingQueue<Event> _outEventQueue;
    private final int _clientId;

    /**
     * @param sock a Socket representing the connection to the other editor
     * @param inEventQueue a BlockingQueue in which to place edit events from
     * the other editor
     * @param outEventQueue a BlockingQueue from which to take events for
     * sending to the other editor
     */
    public EventReceiver(Socket sock,
                         BlockingQueue<Event> inEventQueue,
                         BlockingQueue<Event> outEventQueue,
                         int clientId) {
        _socket        = sock;
        _inEventQueue  = inEventQueue;
        _outEventQueue = outEventQueue;
        _clientId      = clientId;

    }

    public EventReceiver(Socket sock,
                         BlockingQueue<Event> inEventQueue,
                         BlockingQueue<Event> outEventQueue) {
        this(sock, inEventQueue, outEventQueue, IdEvent.NOT_SET);
    }

    public void run() {
        try {
            // Open connection to other editor
            ObjectInputStream objIn
                = new ObjectInputStream( _socket.getInputStream() );

            // Put edit events from the other editor in the queue
            while (true) {
                Event event = (Event) objIn.readObject();
                if (event instanceof IdEvent) {
                    ((IdEvent) event).setSenderId(_clientId);
                }
                _inEventQueue.put(event);

                // Cleanup and close thread if client wants to disconnect
                if (event instanceof DisconnectEvent) {
                    DisconnectEvent disconnectEvent = (DisconnectEvent) event;

                    // Do more cleanup if the other thread is dead already
                    if ( disconnectEvent.shouldClose() ) {
                        _outEventQueue.clear();
                        _socket.close();
                    }
                    // Otherwise say that the others should do more cleanup
                    else {
                        disconnectEvent.setShouldClose();
                        _outEventQueue.put(event);
                    }
                    break;
                }

                // Switch servers if requested
                if (event instanceof ConnectToServerEvent) {
                    ConnectToServerEvent ctse = (ConnectToServerEvent) event;
                    _socket = ctse.getSocket();
                    objIn.close();
                    objIn = new ObjectInputStream( _socket.getInputStream() );
                }
            }
        }
        catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
