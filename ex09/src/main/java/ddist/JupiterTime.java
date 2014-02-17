package ddist;

/**
 * Timestamp used by the Jupiter algorithm.
 */
public class JupiterTime {
    private long _localTime;
    private long _otherTime;

    public JupiterTime() { }

    public void incLocalTime() {
        if (_localTime == Long.MAX_VALUE) {
            throw new IllegalStateException("Maximum value reached.");
        }

        ++_localTime;
    }

    public void incOtherTime() {
        if (_otherTime == Long.MAX_VALUE) {
            throw new IllegalStateException("Maximum value reached.");
        }

        ++_otherTime;
    }

    public boolean is
}
