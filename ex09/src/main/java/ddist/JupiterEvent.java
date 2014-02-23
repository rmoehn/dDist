package ddist;

/**
 * Events exchanged between Jupiter clients and the server.
 */
class JupiterEvent implements Event {
    private static final long serialVersionUID = 8124416967036144277L;

    private final Event _containedEvent;
    private final JupiterTime _timestamp;
    private final boolean _isFromServer;
    private final String _origText;


    protected JupiterEvent(Event event, JupiterTime time, boolean
            isFromServer, String origText) {
        _containedEvent = event;
        _timestamp      = time;
        _isFromServer   = isFromServer;
        _origText       = origText;
    }

    public JupiterEvent(JupiterEvent incompleteEvent, JupiterTime time,
            boolean isFromServer) {
        this(
            incompleteEvent._containedEvent,
            time,
            isFromServer,
            incompleteEvent._origText
        );
    }

    public JupiterEvent(Event event, JupiterTime time, boolean isFromServer) {
        this(event, time, isFromServer, "");
    }

    protected JupiterEvent(Event event, String origText) {
        this(event, new JupiterTime(-1, -1), false, origText);
    }

    /**
     * This event was sent out when its sender already knew about the other
     * event.
     */
    public boolean knowsAbout( JupiterEvent other ) {
        return _timestamp.knowsAbout( other.getTimestamp() );
    }

    public JupiterTime getTimestamp() {
        return _timestamp;
    }

    public Event getContainedEvent() {
        return _containedEvent;
    }

    protected String getOrigText() {
        return _origText;
    }

    protected String getTransformedText() {
        return ((TextChangeEvent) _containedEvent).apply(_origText);
    }

    protected boolean isWithoutTimestamp() {
        return _timestamp.isFake();
    }

    public boolean isFromServer() {
        return _isFromServer;
    }

    @Override
    public String toString() {
        return String.format("%s, fromServer: %b, contains: %s",
                   _timestamp.toString(),
                   _isFromServer,
                   _containedEvent.toString()
               );
    }
}
