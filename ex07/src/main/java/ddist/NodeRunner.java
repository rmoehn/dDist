package ddist;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.*;
import ddist.IChordNode;


public class NodeRunner {

    private static final String CORDNODE = "cordnode";

    public static void main(final String[] args) throws Exception {

        int ringBitSize;
        String localHostname = "";
        int localPort;
        String firstNodeHostname = "";
        int firstNodePort;


        if (args.length == 3) {
            ringBitSize = Integer.parseInt(args[0]);
            ChordNode node = new ChordNode();
            node.newNetwork(ringBitSize);
            createServer(node, args[1], Integer.parseInt(args[2]));
        }

        if (args.length == 5) {
            ringBitSize = Integer.parseInt(args[0]);
            firstNodeHostname = args[3];
            firstNodePort = Integer.parseInt(args[4]);

            IChordNode remoteNode
                = (IChordNode) Naming.lookup("//" + firstNodeHostname + ":"
                                             + firstNodePort + "/" + CORDNODE);
            System.out.printf("Contactet node whith ID %d%n", remoteNode.getID());

            ChordNode node = new ChordNode();
            node.join(remoteNode);
            createServer(node, args[1], Integer.parseInt(args[2]));
        }
    }

    private static void createServer(final IChordNode node, String localHostname, int localPort) throws Exception {
        LocateRegistry.createRegistry(localPort);
        Naming.rebind("//localhost:" + localPort + "/" + CORDNODE, node);

        System.out.printf("New server started on %s:%d With ID %s%n",
                          localHostname, localPort, node.getID());

        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        node.leave();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });


        while (true) {
            System.out.println("#########");
            System.out.println(node.ringToString());
            System.out.println("#########");
            Thread.sleep(3000);
        } // Keep the server alive.
    }
}
