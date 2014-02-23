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

            // Want to disconnect
            if (event instanceof DisconnectEvent) {
                // Pass event on and stop work
                _toDisplayer.add(event);
                break;
            }

            // Generate(op) -- event comes from the DocumentEventCapturer
            JupiterEvent jEvent = (JupiterEvent) event;
            if (jEvent.isWithoutTimestamp()) {
                // apply op locally
                TextChangeEvent localOp
                    = (TextChangeEvent) jEvent.getContainedEvent();
                _toDisplayer.add(localOp);

                // send(op, my Msgs, otherMsgs)
                JupiterEvent jupiterEvent = new JupiterEvent(
                                                jEvent,
                                                _currentTime.getCopy(),
                                                _isServer
                                            );
                _toServer.add(jupiterEvent);

                // add (op, my Msgs) to outgoing
                _outgoing.add(jupiterEvent);

                // myMsgs = myMsgs + 1
                _currentTime.incLocalTime();
            }
            // Receive(msg)
            else {
                JupiterEvent received = (JupiterEvent) jEvent;

                // Discard acknowledged messages.
                while (_outgoing.size() > 0
                        && received.knowsAbout( _outgoing.get(0) )) {
                    _outgoing.remove(0);
                }

                // Transform new message and the ones in the queue.
                for (int i = 0; i < _outgoing.size(); ++i) {
                    String origText = _outgoing.get(i).getOrigText();
                    System.err.println("t_l:  " + origText);
                    System.err.println("t_r:  " + received.getOrigText());
                    System.err.println("l(t): " + _outgoing.get(i).getTransformedText());
                    System.err.println("r(t): " + received.getTransformedText());
                    assert( origText.equals( received.getOrigText() ) );

                    // {msg, outgoing[i]} = xform(msg, outgoing[i])
                    System.err.println("Received: " + received);
                    System.err.println("Outgoing: " + _outgoing.get(i));
                    TransformedPair tp
                        = transformer.transform(received, _outgoing.get(i));
                    System.err.println("Transformed to: " + tp);
                    received = tp.getReceived();
                    _outgoing.set(i, tp.getLocal());

                    System.err.println("r'(l(t)): " + tp.getReceived().getTransformedText());
                    System.err.println("l'(r(t)): " + tp.getLocal().getTransformedText());
                    System.out.println();
                    assert(
                        tp.getReceived().getTransformedText().equals(
                            tp.getLocal().getTransformedText()
                        )
                    );
                }

                // apply msg.op locally
                _toDisplayer.add(received.getContainedEvent());

                // otherMsgs = otherMsgs + 1
                _currentTime.incOtherTime();
            }
        }
    }
}
