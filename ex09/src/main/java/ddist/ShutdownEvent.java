package ddist;

/**
 * Event indicating that the server should hand over to a new server and shut
 * down.
 */
public class ShutdownEvent implements Event {
    private static final long serialVersionUID = -1821652661715101112L;

    public ShutdownEvent() { }
}
