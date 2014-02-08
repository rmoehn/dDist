package ddist;

import java.io.Serializable;

/**
 *
 * @author Jesper Buus Nielsen
 *
 */
public class MyTextEvent implements Serializable {
    private static final long serialVersionUID = -1787964932805068674L;
    
    public MyTextEvent(int offset) {
		this.offset = offset;
	}
	private int offset;
	int getOffset() { return offset; }
}
