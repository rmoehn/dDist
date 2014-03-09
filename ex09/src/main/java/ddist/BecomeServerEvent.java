package ddist;

/**
 * Event indicating that a client should become the new server.
 */
public class BecomeServerEvent implements Event {
    private static final long serialVersionUID = -1273556361704457802L;
    
    private final int _oldClientCount;
    private final String _text;

    public BecomeServerEvent(int oldClientCount, String text) {
        _oldClientCount = oldClientCount;
        _text           = text;
    }

    public int getOldClientCount() {
        return _oldClientCount;
    }

    public String getText() {
        return _text;
    }
}
