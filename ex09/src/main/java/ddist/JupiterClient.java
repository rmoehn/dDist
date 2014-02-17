public class JupiterClient {
    private JupiterTime _currentTime     = new JupiterTime();
    private List<JupiterEvent> _outgoing = new LinkedList<>();

    private BufferedQueue<Event> _inqueue;
    private BufferedQueue<Event> _toServer;
    private BufferedQueue<Event> _toReplayer;

    public JupiterClient(BufferedQueue<Event> inqueue, BufferedQueue<Event>
            toServer, BufferedQueue<Event> _toReplayer) {
        _inqueue    = inqueue;
        _toServer   = toServer;
        _toReplayer = toReplayer;
    }

    public void run() {
        while (true) {
            Event event = inqueue.take();

            // Generate(op)
            if (event instanceof MyTextEvent) {
                MyTextEvent localOp = (MyTextEvent) event;

                // apply op locally
                _toReplayer.add(localOp);

                // send(op, my Msgs, otherMsgs)
                JupiterEvent jupiterEvent
                    = new JupiterEvent(localOp, _currentTime);
                toServer.add(jupiterEvent);

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
                        && received.moreCurrent( _outgoing.get(0) )) {
                    _outgoing.remove(0);
                }

                // Transform new message and the ones in the queue.
                for (int i = 0; i < _outgoing.size(); ++i) {
                    // {msg, outgoing[i]} = xform(msg, outgoing[i])
                    TransformedPair tp
                        = transformer.transform(received, _outgoing.get(i));
                    received = tp.getFirst();
                    _outgoing.set(i, tp.getSecond();
                }

                // apply msg.op locally
                _toReplayer.add(received.getContainedEvent());

                // otherMsgs = otherMsgs + 1
                _currentTime.incOtherTime();
            }
        }
    }
}
