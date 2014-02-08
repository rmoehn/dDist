package ddist;

/**
 * Event indicating that someone wants to disconnect.
 */
public class DisconnectEvent extends MyTextEvent {
    private static final long serialVersionUID = -3411878142976145233L;
    
    // Indicates whether threads that see this event may close the socket
    private boolean _shouldClose = false;

    public DisconnectEvent() {
        super(-1);
    }

    public void setShouldClose() {
        _shouldClose = true;
    }

    public boolean shouldClose() {
        return _shouldClose;
    }
}
