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
    private BlockingQueue<Event> _toReplayer;
    private final boolean _isServer; // Makes sense in the 1-1 case

    public JupiterClient(BlockingQueue<Event> inqueue, BlockingQueue<Event>
            toServer, BlockingQueue<Event> toReplayer, boolean isServer) {
        _inqueue    = inqueue;
        _toServer   = toServer;
        _toReplayer = toReplayer;
        _isServer   = isServer;
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
            if (event instanceof MyTextEvent) {
                MyTextEvent localOp = (MyTextEvent) event;

                // apply op locally
                _toReplayer.add(localOp);

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
                    TransformedPair tp
                        = transformer.transform(received, _outgoing.get(i));
                    received = tp.getReceived();
                    _outgoing.set(i, tp.getLocal());
                }

                // apply msg.op locally
                _toReplayer.add(received.getContainedEvent());

                // otherMsgs = otherMsgs + 1
                _currentTime.incOtherTime();
            }
        }
    }
}
