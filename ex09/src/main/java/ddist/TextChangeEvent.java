package ddist;

/**
 *
 * @author Jesper Buus Nielsen
 *
 */
public class TextChangeEvent implements Event {
    private static final long serialVersionUID = -1787964932805068674L;

    public TextChangeEvent(int offset) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }
}
