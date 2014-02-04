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
            ChordNode node = new ChordNode(ringSize);
            try {
                IChordNode stub = (IChordNode) UnicastRemoteObject.exportObject(node, localPort);
                Registry registry = LocateRegistry.getRegistry();
                registry.rebind(Integer.toString(node.getID()), stub);
                System.out.printf("New server started on %s:%d%n", localHostname, localPort);
            } catch(Exception e) {
                System.err.println("CordNode exception:");
                e.printStackTrace();
            }
        }

        if (args.length == 5) {
            firstNodeHostname = args[3];
            firstNodePort = Integer.parseInt(args[4]);
            //Create server and node.join(localHostname, localPort, firstNodeHostname, firstNodePort);
        }

    }
}
