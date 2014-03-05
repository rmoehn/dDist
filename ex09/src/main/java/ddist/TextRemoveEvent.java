package ddist;

public class TextRemoveEvent extends TextChangeEvent {
    private static final long serialVersionUID = -6053913376936143486L;

    private int length;

    public TextRemoveEvent(int offset, int length) {
        super(offset);
        this.length = length;
    }

    public int getLength() { return length; }

    @Override
    public String toString() {
        return String.format(
                             "At %4d remove %4d characters.",
                             this.getOffset(),
                             this.length
                             );
    }
}
