package ddist;

import java.awt.EventQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * Takes the event recorded by the DocumentEventCapturer and replays
 * them in a JTextArea.
 *
 * @author Jesper Buus Nielsen
 *
 */
public class EventReplayer implements Runnable {

    private BlockingQueue<Event> eventQueue;
    private JTextArea area;
    private JFrame frame;

    /**
     * @param eventQueue the blocking queue from which to take events to
     * replay them in the on the second argument
     * @param area the text area in which to replay the events
     * @param frame the overall frame of the program (might be done better)
     */
    public EventReplayer(BlockingQueue<Event> eventQueue,
                         JTextArea area, JFrame frame) {
        this.eventQueue = eventQueue;
        this.area = area;
        this.frame = frame;
    }

    public void run() {
        boolean wasInterrupted = false;
        while (!wasInterrupted) {
            try {
                Event mte = eventQueue.take();
                if (mte instanceof TextInsertEvent) {
                    final TextInsertEvent tie = (TextInsertEvent)mte;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    area.insert(tie.getText(), tie.getOffset());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    /* We catch all axceptions, as an uncaught
                                     * exception would make the EDT unwind,
                                     * which is now healthy.
                                     */
                                }
                            }
                        });
                } else if (mte instanceof TextRemoveEvent) {
                    final TextRemoveEvent tre = (TextRemoveEvent)mte;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    area.replaceRange(null, tre.getOffset(),
                                                      tre.getOffset() +
                                                      tre.getLength());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    /* We catch all axceptions, as an uncaught
                                     * exception would make the EDT unwind,
                                     * which is now healthy.
                                     */
                                }
                            }
                        });
                }
                else if (mte instanceof DisconnectEvent) {
                    JOptionPane.showMessageDialog(frame, "Disconnected.");
                    frame.setTitle("Disconnected");
                    area.setText("");
                }
                else {
                    System.err.println("Illegal event received.");
                    System.exit(1);
                }
            } catch (Exception _) {
                wasInterrupted = true;
            }
        }
        System.out.println(
            "I'm the thread running the EventReplayer, now I die!");
    }
}
