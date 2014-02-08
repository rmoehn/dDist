package ddist;

public class QuizServer {
    private final int _port;
    private final BlockingQueue<QA> _qaQueue = new LinkedBlockingQueue<>();

    /**
     * @param port the port on which this server should accept a client
     */
    public QuizServer(int port) {
        _port = port;
    }

    public void run() {
        // Create input stream for the command line
        BufferedReader stdin = new BufferedReader(
                                   new InputStreamReader(
                                       System.in
                                   )
                               );

        // Create socket and wait for a client
        ServerSocket servSock = new ServerSocket(port);
        Socket sock           = servSock.accept();

        // Create input and output streams on the socket
        ObjectInputStream objIn
            = new ObjectInputStream( sock.getInputStream() );
        ObjectOutputStream objOut
            = new ObjectOutputStream( sock.getOutputStream() );

        // Start thread for receiving QAs
        Thread inManager = new Thread() {
            public void run() {
                // Add incoming questions to the queue
                while ( QA qa = (QA) objIn.readObject() ) {
                    _qaQueue.put(qa);
                }
            }
        };

        // Wait for QAs in the queue
        while ( QA qa = _qaQueue.take() ) {
            // Print question
            System.out.println("Q: " + qa.getQuestion());
            System.out.println("A? ");

            // Read answer
            String answer = stdin.readLine();

            // Put it into the QA and send back
            qa.setAnswer(answer);
            objOut.writeObject(qa);
        }
    }
}
