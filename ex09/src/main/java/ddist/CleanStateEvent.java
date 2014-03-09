package ddist;

/**
 * Event indicating that the old server won't send any more JupiterEvents.
 */
public class CleanStateEvent implements Event {
    private static final long serialVersionUID = 1287655970046626867L;

    public CleanStateEvent() { }
}
