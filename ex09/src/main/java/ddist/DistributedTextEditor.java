package ddist;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;

public class DistributedTextEditor extends JFrame {
    private static final long serialVersionUID = 4813L;

    private JTextArea area1 = new JTextArea(10,120);
    private JTextField _listenIp = new JTextField("localhost", 15);
    private JTextField _listenPort = new JTextField("20000", 5);

    private JTextField _remoteIp = new JTextField("localhost", 15);
    private JTextField _remotePort = new JTextField("20000", 5);

    /*
     * Queue for holding events coming in from the upper text area or the
     * other editor to be sent to the Jupiter algorithm.
     */
    private BlockingQueue<Event> _toLocalClient = new LinkedBlockingQueue<>();
    private BlockingQueue<Event> _localClientToDisplayer
        = new LinkedBlockingQueue<>();

    private Client _localClient;

    private EventDisplayer eventDisplayer;
    private Thread eventDisplayerThread;

    private JFileChooser dialog =
        new JFileChooser(System.getProperty("user.dir"));

    private String currentFile = "Untitled";
    private boolean changed = false;
    private DocumentEventCapturer dec
        = new DocumentEventCapturer(_toLocalClient);

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

        JPanel addressPanel
            = new JPanel( new FlowLayout(FlowLayout.LEFT, 1, 3) );
        content.add(addressPanel, BorderLayout.SOUTH);

        addressPanel.add(new JLabel("Listen on: "));
        addressPanel.add(_listenIp);
        addressPanel.add(new JLabel(":"));
        addressPanel.add(_listenPort);
        addressPanel.add(new JLabel("  Connect to: ")); // Sorry for the hack.
        addressPanel.add(_remoteIp);
        addressPanel.add(new JLabel(":"));
        addressPanel.add(_remotePort);

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
        this.setLocationByPlatform(true);
        pack();
        area1.addKeyListener(k1);
        setTitle( makeTitle("Disconnected") );
        setVisible(true);

        eventDisplayer = new EventDisplayer(dec, _localClientToDisplayer, area1, this);
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
                // TODO: Retrieve address from text field
                String listenAddress = null;
                try {
                    listenAddress = InetAddress.getLocalHost().getHostAddress();
                }
                catch (UnknownHostException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
                final int listenPort = Integer.parseInt(_listenPort.getText());
                setTitle(
                    makeTitle(
                        String.format(
                            "I'm listening on %s:%d.",
                            listenAddress,
                            listenPort
                        )
                    )
                );
                Server server = new Server(listenPort);
                server.start();

                Socket clientSocket = null;
                try {
                    clientSocket = new Socket(listenAddress, Integer.parseInt(_remotePort.getText()));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DistributedTextEditor.this,
                                                  "Connecting failed.");
                    ex.printStackTrace();
                    return;
                }

                startClient(clientSocket, listenPort, true);

                // Give the editor a better title
                /*                setTitle(String.format("Connected to %s:%d.",
                                  socket.getInetAddress().toString(),
                                  socket.getPort()));*/
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
                String address = _remoteIp.getText();
                int port = Integer.parseInt(_remotePort.getText() );
                setTitle( makeTitle(
                    String.format("Connecting to %s:%d...", address, port)));

                // Initiate connection with other editor
                Socket socket = null;
                try {
                    socket = new Socket(address, port);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DistributedTextEditor.this,
                                                  "Connecting failed.");
                    ex.printStackTrace();
                    return;
                }

                // Set up the event sending and receiving
                startClient(
                    socket,
                    Integer.parseInt(_listenPort.getText()),
                    false
                );

                // Give the editor a better title
                setTitle( makeTitle(
                     String.format("Connected to %s:%d.", address, port)));
            }
        };

    Action Disconnect = new AbstractAction("Disconnect") {
            private static final long serialVersionUID = 983498L;

            public void actionPerformed(ActionEvent e) {
                _localClient.sendDisconnect();
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

    private String makeTitle(String title) {
        return "DTE: " + title;
    }

    private void startClient(Socket socket, int listenPort,
            boolean isRunningServer) {
        _localClient = new Client(
                           _toLocalClient,
                           _localClientToDisplayer,
                           socket,
                           listenPort,
                           isRunningServer
                       );
        _localClient.start();
        dec.enableEventGeneration();
    }

    public static void main(String[] arg) {
        new DistributedTextEditor();
    }
}
