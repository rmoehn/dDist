package ddist;

import java.io.Serializable;

/**
 * Timestamp used by the Jupiter algorithm.
 */
public class JupiterTime implements Serializable {
    private static final long serialVersionUID = -335296904930150389L;
    
    private long _localTime;
    private long _otherTime;

    public JupiterTime() { }

    /**
     * A received time/state (this) knows about a local time/state (other).
     */
    public boolean knowsAbout(JupiterTime other) {
        return _otherTime > other._localTime;
    }

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
}
