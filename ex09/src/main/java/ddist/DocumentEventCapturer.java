package ddist;

import java.util.concurrent.BlockingQueue;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 *
 * This class captures and remembers the text events of the given document on
 * which it is put as a filter. Normally a filter is used to put restrictions
 * on what can be written in a buffer. In out case we just use it to see all
 * the events and make a copy.
 *
 * @author Jesper Buus Nielsen
 *
 */
public class DocumentEventCapturer extends DocumentFilter {
    private boolean isGenerateEvents;

    /*
     * We are using a blocking queue for two reasons:
     * 1) They are thread safe, i.e., we can have two threads add and take elements
     *    at the same time without any race conditions, so we do not have to do
     *    explicit synchronization.
     * 2) It gives us a member take() which is blocking, i.e., if the queue is
     *    empty, then take() will wait until new elements arrive, which is what
     *    we want, as we then don't need to keep asking until there are new elements.
     */
    protected BlockingQueue<Event> eventHistory;

    /**
     * @param eventHistory the queue this object should write the captured
     * events to
     */
    public DocumentEventCapturer(BlockingQueue<Event> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public void insertString(FilterBypass fb, int offset,
			     String str, AttributeSet a)
	throws BadLocationException {
	/* Queue a copy of the event or modify the textarea */
        if (isGenerateEvents) {
            Document doc = fb.getDocument();
            eventHistory.add(
                new JupiterEvent(
                    new TextInsertEvent(offset, str),
                    doc.getText(0, doc.getLength())
                )
            );
        }
        else {
            super.insertString(fb, offset, str, a);
        }
    }

    public void remove(FilterBypass fb, int offset, int length)
	throws BadLocationException {
	/* Queue a copy of the event or modify the textarea */
        if (isGenerateEvents) {
            Document doc = fb.getDocument();
            eventHistory.add(
                new JupiterEvent(
                    new TextRemoveEvent(offset, length),
                    doc.getText(0, doc.getLength())
                )
            );
        }
        else {
            super.remove(fb, offset, length);
        }
    }

    public void replace(FilterBypass fb, int offset,
			int length,
			String str, AttributeSet a)
	throws BadLocationException {
        /* Queue a copy of the event or modify the text */
        Document doc = fb.getDocument();
        if (isGenerateEvents) {
            if (length > 0) {
                eventHistory.add(
                    new JupiterEvent(
                        new TextRemoveEvent(offset, length),
                        doc.getText(0, doc.getLength())
                    )
                );
            }
            eventHistory.add(
                new JupiterEvent(
                    new TextInsertEvent(offset, str),
                    doc.getText(0, doc.getLength())
                )
            );
        }
        else {
            super.replace(fb, offset, length, str, a);
        }
    }

    public void enableEventGeneration() {
        this.isGenerateEvents = true;
    }

    public void disableEventGeneration() {
        this.isGenerateEvents = false;
    }
}
