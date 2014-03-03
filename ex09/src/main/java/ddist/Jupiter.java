package ddist;

import java.util.LinkedList;
import java.util.List;

/**
 * Central mechanism for optimistic synchronisation. Described in
 * Nichols, Curtis, Dixon, Lamping: High-Latency, Low-Bandwidth Windowing in
 * the Jupiter Collaboration System. UIST 95 Pittsburgh PA USA, Proceedings.
 */
public class Jupiter {
    private JupiterTime _currentTime     = new JupiterTime();
    private List<JupiterEvent> _outgoing = new LinkedList<>();
    private Transformer transformer      = new Transformer();

    private final boolean _isServer;

    public Jupiter(boolean isServer) {
        _isServer = isServer;
    }

    public JupiterEvent generate(TextChangeEvent event) {
        JupiterEvent jupiterEvent
            = new JupiterEvent(event, _currentTime.getCopy(), _isServer);
        // add (op, my Msgs) to outgoing
        _outgoing.add(jupiterEvent);

        // myMsgs = myMsgs + 1
        _currentTime.incLocalTime();

        return jupiterEvent;
    }

    public JupiterEvent receive(JupiterEvent event) {
        // Discard acknowledged messages.
        while (_outgoing.size() > 0
               && event.knowsAbout( _outgoing.get(0) )) {
            _outgoing.remove(0);
        }

        // Transform new message and the ones in the queue.
        for (int i = 0; i < _outgoing.size(); ++i) {
            // {msg, outgoing[i]} = xform(msg, outgoing[i])
            TransformedPair tp
                = transformer.transform(event, _outgoing.get(i));
            event = tp.getReceived();
            _outgoing.set(i, tp.getLocal());
        }

        // otherMsgs = otherMsgs + 1
        _currentTime.incOtherTime();

        return event;
    }
}
