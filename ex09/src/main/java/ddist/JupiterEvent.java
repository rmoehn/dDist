package ddist;

/**
 * Events exchanged between Jupiter clients and the server.
 */
class JupiterEvent implements Event {
    private final Event _containedEvent;
    private final JupiterTime _timestamp;

    public JupiterEvent(Event event, JupiterTime time) {
        _containedEvent = event;
        _timestamp      = time;
    }

    public Event getContainedEvent() {
        return _containedEvent;
    }

    public boolean moreCurrent( JupiterEvent other ) {
        return _timestamp.getOther
    }
}
