package ddist;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import ddist.IChordNode;

public class NodeRunner {

    public static void main(final String[] args) {
        int ringSize;
        String localHostname = "";
        int localPort;
        String firstNodeHostname = "";
        int firstNodePort;


        if (args.length == 3) {
            ringSize = Integer.parseInt(args[0]);
            localHostname = args[1];
            localPort = Integer.parseInt(args[2]);
            IChordNode node = new ChordNode(ringSize);

            try {
                node.newNetwork();
                IChordNode stub = (IChordNode) UnicastRemoteObject.exportObject(node, 1099);
                Registry registry = LocateRegistry.createRegistry(1099);
                
                String[] regList = registry.list();
                System.out.printf("Registry: %n");
                for (String reg : regList) {
                    System.out.printf("Item: %s%n", reg);
                }

                
                registry.rebind("CORDNAME", node);

                String[] regList2 = registry.list();
                System.out.printf("Registry: %n");
                for (String reg : regList2) {
                    System.out.printf("Item: %s%n", reg);
                }

                System.out.printf("New server started on %s:%d With ID %s%n", localHostname, localPort,
                                  node.getID());
            } catch(Exception e) {
                System.err.println("CordNode exception:");
                e.printStackTrace();
            }
        }

        if (args.length == 5) {
            ringSize = Integer.parseInt(args[0]);
            localHostname = args[1];
            localPort = Integer.parseInt(args[2]);
            firstNodeHostname = args[3];
            firstNodePort = Integer.parseInt(args[4]);
            ChordNode node = new ChordNode(ringSize);
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                String[] regList = registry.list();
                for (String reg : regList) {
                     System.out.printf("Item: %s%n", reg);
                 }
                // IChordNode stub = (IChordNode) registry.lookup("CORDNAME");
                // System.out.printf("Contactet node whith ID %d%n", stub.getID());

                //Create server and node.join(localHostname, localPort, firstNodeHostname, firstNodePort);
            } catch(Exception e) {
                System.err.println("CordNode exception:");
                e.printStackTrace();
            }

        }

    }
}
