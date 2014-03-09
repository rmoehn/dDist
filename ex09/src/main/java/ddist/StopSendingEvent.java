package ddist;

/**
 * Event indicating that the clients shouldn't send new JupiterEvents.
 */
public class StopSendingEvent implements Event {
    private static final long serialVersionUID = -6601044170101267327L;

    public StopSendingEvent() { }
}
