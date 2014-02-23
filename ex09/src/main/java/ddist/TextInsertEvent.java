package ddist;

/**
 *
 * @author Jesper Buus Nielsen
 *
 */
public class TextInsertEvent extends TextChangeEvent {
    private static final long serialVersionUID = -6230877975439784299L;

    private String text;

	public TextInsertEvent(int offset, String text) {
		super(offset);
		this.text = text;
	}
	public String getText() { return text; }

    protected String apply(String text) {
        return (new StringBuffer(text)).insert(this.getOffset(), this.text)
                                       .toString();
    }

    @Override
    public String toString() {
        return String.format(
                   "At %4d insert %s.", this.getOffset(), this.text);
    }
}

