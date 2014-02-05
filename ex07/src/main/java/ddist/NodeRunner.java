package ddist;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.*;
import ddist.IChordNode;

public class NodeRunner {
    public static void main(final String[] args) throws RemoteException {
        int PORT_NUMBER = 40499;
        int ringSize;
        String localHostname = "";
        int localPort;
        String firstNodeHostname = "";
        int firstNodePort;


        if (args.length == 3) {
            ringSize = Integer.parseInt(args[0]);
            localHostname = args[1];
            localPort = Integer.parseInt(args[2]);
            ChordNode node = new ChordNode(ringSize);

            try {
                node.newNetwork();
                LocateRegistry.createRegistry(PORT_NUMBER);
                Naming.rebind("//localhost:" + PORT_NUMBER + "/firstNode", node);

                System.out.printf("New server started on %s:%d With ID %s%n", localHostname, localPort,
                                  node.getID());
            } catch(Exception e) {
                System.err.println("CordNode exception:");
                e.printStackTrace();
            }

            while (true) {}
        }

        if (args.length == 5) {
            ringSize = Integer.parseInt(args[0]);
            localHostname = args[1];
            localPort = Integer.parseInt(args[2]);
            firstNodeHostname = args[3];
            firstNodePort = Integer.parseInt(args[4]);
            ChordNode node = new ChordNode(ringSize);
            try {
                IChordNode remoteNode
                    = (IChordNode) Naming.lookup("//localhost:" + PORT_NUMBER + "/firstNode");
                System.out.printf("Contactet node whith ID %d%n", remoteNode.getID());

                //Create server and node.join(localHostname, localPort, firstNodeHostname, firstNodePort);
            } catch(Exception e) {
                System.err.println("CordNode exception:");
                e.printStackTrace();
            }

        }

    }
}
