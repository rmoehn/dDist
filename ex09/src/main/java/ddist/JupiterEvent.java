package ddist;

/**
 * Events exchanged between Jupiter clients and the server.
 */
class JupiterEvent implements Event {
    private static final long serialVersionUId = 8124416967036144277L;
    public static final int NOT_SET = -1;

    private final Event _containedEvent;
    private final JupiterTime _timestamp;
    private final boolean _isFromServer;
    private int _senderId;

    public JupiterEvent(Event event, JupiterTime time, boolean isFromServer) {
        _containedEvent = event;
        _timestamp      = time;
        _isFromServer   = isFromServer;
        _senderId       = NOT_SET;
    }

    public void setSenderId(int senderId) {
        _senderId = senderId;
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

    public int getSenderId() {
        if (_senderId == NOT_SET) {
            throw new IllegalStateException("senderId is not set in event.");
        }
        return _senderId;
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
