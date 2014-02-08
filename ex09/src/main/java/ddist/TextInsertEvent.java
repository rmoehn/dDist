package ddist;

/**
 *
 * @author Jesper Buus Nielsen
 *
 */
public class TextInsertEvent extends MyTextEvent {
    private static final long serialVersionUID = -6230877975439784299L;
    
    private String text;

	public TextInsertEvent(int offset, String text) {
		super(offset);
		this.text = text;
	}
	public String getText() { return text; }
}

