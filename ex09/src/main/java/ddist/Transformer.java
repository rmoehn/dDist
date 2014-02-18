package ddist;

public class Transformer {
    public Transformer() { }

    public TransformedPair transform(JupiterEvent received, JupiterEvent
            local) {
        return new TransformedPair(received, local);
    }
}
