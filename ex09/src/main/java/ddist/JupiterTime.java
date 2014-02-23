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

    protected JupiterTime(long localTime, long otherTime) {
        _localTime = localTime;
        _otherTime = otherTime;
    }

    /**
     * A received time/state (this) knows about a local time/state (other).
     */
    public boolean knowsAbout(JupiterTime other) {
        return _otherTime > other._localTime;
    }

    public JupiterTime getCopy() {
        return new JupiterTime(_localTime, _otherTime);
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

    protected boolean isFake() {
        return (_localTime == -1) && (_otherTime == -1);
    }

    @Override
    public String toString() {
        return String.format("(%4d, %4d)", _localTime, _otherTime);
    }
}
