package ddist;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class JupiterClient implements Runnable {
    private JupiterTime _currentTime     = new JupiterTime();
    private List<JupiterEvent> _outgoing = new LinkedList<>();
    private Transformer transformer      = new Transformer();

    private BlockingQueue<Event> _inqueue;
    private BlockingQueue<Event> _toServer;
    private BlockingQueue<Event> _toDisplayer;
    private final boolean _isServer; // Makes sense in the 1-1 case

    public JupiterClient(BlockingQueue<Event> inqueue, BlockingQueue<Event>
            toServer, BlockingQueue<Event> toDisplayer, boolean isServer) {
        _inqueue     = inqueue;
        _toServer    = toServer;
        _toDisplayer = toDisplayer;
        _isServer    = isServer;
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

                // send(op, my Msgs, otherMsgs)
                JupiterEvent jupiterEvent
                    = new JupiterEvent(localOp, _currentTime.getCopy(), _isServer);
                _toServer.add(jupiterEvent);

                // add (op, my Msgs) to outgoing
                _outgoing.add(jupiterEvent);

                // myMsgs = myMsgs + 1
                _currentTime.incLocalTime();
            }
            // Receive(msg)
            else if (event instanceof JupiterEvent) {
                JupiterEvent received = (JupiterEvent) event;

                // Discard acknowledged messages.
                while (_outgoing.size() > 0
                        && received.knowsAbout( _outgoing.get(0) )) {
                    _outgoing.remove(0);
                }

                // Transform new message and the ones in the queue.
                for (int i = 0; i < _outgoing.size(); ++i) {
                    // {msg, outgoing[i]} = xform(msg, outgoing[i])
                    System.err.println("Received: " + received);
                    System.err.println("Outgoing: " + _outgoing.get(i));
                    TransformedPair tp
                        = transformer.transform(received, _outgoing.get(i));
                    System.err.println("Transformed to: " + tp);
                    received = tp.getReceived();
                    _outgoing.set(i, tp.getLocal());
                }

                // apply msg.op locally
                _toDisplayer.add(received.getContainedEvent());

                // otherMsgs = otherMsgs + 1
                _currentTime.incOtherTime();
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
