package ddist;

public class QuizServer {
    private final int _port;
    private final BlockingQueue<QA> _qaQueue = new LinkedBlockingQueue<>();

    /**
     * @param port the port on which this server should accept new clients
     */
    public QuizServer(int port) {
        _port = port;
    }

    public void run() {
        // Create socket and wait for clients

        // Create input and output streams on the socket

        // Start thread for receiving QAs
        //      Wait for incoming question
        //      Add it to the queue
        //      Repeat

        // Create input and output streams for the command line

        // Wait for items in the queue
        //      Remove QA
        //      Print question
        //      Wait for input
        //      Write input to QA
        //      Send QA back to client
        //      Repeat
    }
}
