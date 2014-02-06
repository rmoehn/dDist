package ddist;

import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import ddist.IChordNode;

public class NodeRunner {
    private static final String CHORDNODE = "chordnode";

    public static void main(final String[] args) throws Exception {
        // Check arguments
        if ((args.length != 2) && (args.length != 4)) {
            System.err.println("Wrong number of arguments.");
            System.exit(1);
        }

        // Create node for this process
        int ringBitSize     = Integer.parseInt(args[0]);
        ChordNode localNode = new ChordNode();

        // Start as first node in ring
        if (args.length == 2) {
            localNode.newNetwork(ringBitSize);
        }
        // Join network through node specified by args[2]:args[3]
        else if (args.length == 4) {
            IChordNode remoteNode = (IChordNode) Naming.lookup(
                String.format("//%s:%s/%s", args[2], args[3], CHORDNODE) );
            System.out.println(
                "Contacted node whith ID " + remoteNode.getID() );

            localNode.join(remoteNode);
        }

        // Make local node accessible on localhost:args[1]
        createServer( localNode, Integer.parseInt(args[1]) );
    }

    private static void createServer(final IChordNode node, int localPort)
            throws Exception {
        // Put node in a new registry
        LocateRegistry.createRegistry(localPort);
        Naming.rebind(
            String.format("//localhost:%d/%s", localPort, CHORDNODE),
            node
        );
        System.out.printf(
            "New server started on localhost:%d with ID %d%n",
            localPort, node.getID()
        );

        // Make nodes leave cleanly if the JVM is terminated
        Runtime.getRuntime().addShutdownHook( new Thread() {
            public void run() {
                try {
                    node.leave();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } );


        // Continuously print network information, keeping server alive
        while (true) {
            System.out.println("#########");
            System.out.println( node.ringToString() );
            System.out.println("#########");
            Thread.sleep(3000);
        }
    }
}
