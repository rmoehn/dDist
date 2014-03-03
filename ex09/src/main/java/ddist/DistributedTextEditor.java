package ddist;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;

public class DistributedTextEditor extends JFrame {
    private static final long serialVersionUID = 4813L;

    private JTextArea area1 = new JTextArea(10,120);
    private JTextField ipaddress = new JTextField("localhost");
    private JTextField portNumber = new JTextField("20000");

    /*
     * Queue for holding events coming in from the upper text area or the
     * other editor to be sent to the Jupiter algorithm.
     */
    private BlockingQueue<Event> inEventQueue = new LinkedBlockingQueue<>();

    /*
     * Queue for holding events coming from the upper text area to be sent to
     * the other editor.
     */
    private BlockingQueue<Event> outEventQueue = new LinkedBlockingQueue<>();

    /*
     * Queue on which the Jupiter algorithm sends processed events to the
     * upper text area.
     */
    private BlockingQueue<Event> displayEventQueue 
        = new LinkedBlockingQueue<>();

    private EventDisplayer eventDisplayer;
    private Thread eventDisplayerThread;

    private JFileChooser dialog =
    		new JFileChooser(System.getProperty("user.dir"));

    private String currentFile = "Untitled";
    private boolean changed = false;
    private DocumentEventCapturer dec
        = new DocumentEventCapturer(inEventQueue);

    public DistributedTextEditor() {
    	area1.setFont(new Font("Monospaced",Font.PLAIN,12));
    	((AbstractDocument)area1.getDocument()).setDocumentFilter(dec);

    	Container content = getContentPane();
    	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    	JScrollPane scroll1 =
    			new JScrollPane(area1,
    					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
    					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    	content.add(scroll1,BorderLayout.CENTER);

		content.add(ipaddress,BorderLayout.CENTER);
		content.add(portNumber,BorderLayout.CENTER);

	JMenuBar JMB = new JMenuBar();
	setJMenuBar(JMB);
	JMenu file = new JMenu("File");
	JMenu edit = new JMenu("Edit");
	JMB.add(file);
	JMB.add(edit);

	file.add(Listen);
	file.add(Connect);
	file.add(Disconnect);
	file.addSeparator();
	file.add(Save);
	file.add(SaveAs);
	file.add(Quit);

	edit.add(Copy);
	edit.add(Paste);
	edit.getItem(0).setText("Copy");
	edit.getItem(1).setText("Paste");

	Save.setEnabled(false);
	SaveAs.setEnabled(false);

	setDefaultCloseOperation(EXIT_ON_CLOSE);
	pack();
	area1.addKeyListener(k1);
	setTitle("Disconnected");
	setVisible(true);

	eventDisplayer = new EventDisplayer(dec, displayEventQueue, area1, this);
	eventDisplayerThread = new Thread(eventDisplayer);
	eventDisplayerThread.start();
    }

    private KeyListener k1 = new KeyAdapter() {
	    public void keyPressed(KeyEvent e) {
		changed = true;
		Save.setEnabled(true);
		SaveAs.setEnabled(true);
	    }
	};

    Action Listen = new AbstractAction("Listen") {
        private static final long serialVersionUID = 3098L;

	    public void actionPerformed(ActionEvent e) {
	    	saveOld();

            // Prepare for connection
	    	area1.setText("");
	    	changed = false;
	    	Save.setEnabled(false);
	    	SaveAs.setEnabled(false);

            // Display information about the listening
            String address = null;
            try {
                address = InetAddress.getLocalHost().getHostAddress();
            }
            catch (UnknownHostException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            final int port = Integer.parseInt(portNumber.getText());
	    	setTitle(
                String.format("I'm listening on %s:%d.", address, port));

            // Asynchronously wait for a connection
            new Thread( new Runnable() {
                public void run() {
                    // Wait for an incoming connection
                    Socket socket = null;
                    try {
                        ServerSocket servSock = new ServerSocket(port);
                        socket = servSock.accept();
                        servSock.close();
                    }
                    catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                            DistributedTextEditor.this, "Cannot listen.");
                        ex.printStackTrace();
                        return;
                    }

                    // Set up the event sending and receiving
                    startCommunication(socket, true);

                    // Give the editor a better title
                    setTitle(
                        String.format(
                            "Connected to %s:%d.",
                            socket.getInetAddress().toString(),
                            socket.getPort()
                        )
                    );
                }
            } ).start();
	    }
	};

    Action Connect = new AbstractAction("Connect") {
        private static final long serialVersionUID = 135098L;

	    public void actionPerformed(ActionEvent e) {
            // Prepare for connection
	    	saveOld();
	    	area1.setText("");
	    	changed = false;
	    	Save.setEnabled(false);
	    	SaveAs.setEnabled(false);

            // Find out with whom to connect
            String address = ipaddress.getText();
            int port = Integer.parseInt( portNumber.getText() );
	    	setTitle(
	    	    String.format("Connecting to %s:%d...", address, port));

            // Initiate connection with other editor
            Socket socket = null;
            try {
                socket = new Socket(address, port);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    DistributedTextEditor.this, "Connecting failed.");
                ex.printStackTrace();
                return;
            }

            // Set up the event sending and receiving
            startCommunication(socket, false);

            // Give the editor a better title
	    	setTitle(
	    	    String.format("Connected to %s:%d.", address, port));
	    }
	};

    Action Disconnect = new AbstractAction("Disconnect") {
        private static final long serialVersionUID = 983498L;

	    public void actionPerformed(ActionEvent e) {
            // Initiate disconnecting process
            outEventQueue.add( new DisconnectEvent() );
	    }
	};

    Action Save = new AbstractAction("Save") {
        private static final long serialVersionUID = 409098L;

	    public void actionPerformed(ActionEvent e) {
		if(!currentFile.equals("Untitled"))
		    saveFile(currentFile);
		else
		    saveFileAs();
	    }
	};

    Action SaveAs = new AbstractAction("Save as...") {
        private static final long serialVersionUID = 2848761098L;

	    public void actionPerformed(ActionEvent e) {
	    	saveFileAs();
	    }
	};

    Action Quit = new AbstractAction("Quit") {
        private static final long serialVersionUID = 92497045L;

	    public void actionPerformed(ActionEvent e) {
	    	saveOld();
	    	System.exit(0);
	    }
	};

    ActionMap m = area1.getActionMap();

    Action Copy = m.get(DefaultEditorKit.copyAction);
    Action Paste = m.get(DefaultEditorKit.pasteAction);

    private void saveFileAs() {
	if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
	    saveFile(dialog.getSelectedFile().getAbsolutePath());
    }

    private void saveOld() {
    	if(changed) {
	    if(JOptionPane.showConfirmDialog(this, "Would you like to save "+ currentFile +" ?","Save",JOptionPane.YES_NO_OPTION)== JOptionPane.YES_OPTION)
		saveFile(currentFile);
    	}
    }

    private void saveFile(String fileName) {
	try {
	    FileWriter w = new FileWriter(fileName);
	    area1.write(w);
	    w.close();
	    currentFile = fileName;
	    changed = false;
	    Save.setEnabled(false);
	}
	catch(IOException e) {
	}
    }

    /**
     * Start threads for handling the transportation of events between the
     * network and the local event queues.
     */
    private void startCommunication(Socket socket, boolean isServer) {
        // Start thread containing the Jupiter client/server
        ClientEventDistributor jc = new ClientEventDistributor(
                               inEventQueue,
                               outEventQueue,
                               displayEventQueue
                           );
        Thread jupiterThread = new Thread(jc);
        jupiterThread.start();

        // Start thread for adding incoming events to the inqueue
        EventReceiver rec
            = new EventReceiver(socket, inEventQueue, outEventQueue);
        Thread receiverThread = new Thread(rec);
        receiverThread.start();

        // Start thread for taking outgoing events from the outqueue
        EventSender sender
            = new EventSender(socket, outEventQueue);
        Thread senderThread = new Thread(sender);
        senderThread.start();

        dec.enableEventGeneration();
    }

    public static void main(String[] arg) {
    	new DistributedTextEditor();
    }

}
