package ddist;

public class TransformedPair {
    private final JupiterEvent _received;
    private final JupiterEvent _local;

    public TransformedPair(JupiterEvent received, JupiterEvent local) {
        _received = received;
        _local    = local;
    }

    public JupiterEvent getReceived() {
        return _received;
    }

    public JupiterEvent getLocal() {
        return _local;
    }
}
