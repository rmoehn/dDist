package ddist;

protected class DebugTextEvent implements Event {
    private final TextChangeEvent _textChangeEvent;
    private final String _origText;

    protected DebugTextEvent(TextChangeEvent tce, String ot) {
        _textChangeEvent = tce;
        _origText        = ot;
    }

    protected String applyEventToText() {
        return tce.apply(_origText;
    }

    protected TextChangeEvent getContainedEvent() {
        return _textChangeEvent;
    }
}
