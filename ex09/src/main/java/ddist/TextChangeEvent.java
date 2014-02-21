package ddist;

/**
 *
 * @author Jesper Buus Nielsen
 *
 */
public abstract class TextChangeEvent implements Event {
    private static final long serialVersionUID = -1787964932805068674L;

    public TextChangeEvent(int offset, String origText) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }

    protected String apply(String text);
}
