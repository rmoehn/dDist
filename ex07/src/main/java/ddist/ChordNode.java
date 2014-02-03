package ddist;

import ddist.ChordRing;
import java.util.Comparator;

public class ChordNode implements IChordNode, Comparable<ChordNode> {
    private ChordNode successor;
    private ChordNode predecessor;
    private final ChordRing ring;
    private int id;
    private String hostname;
    private int port;

    public ChordNode(String hostname, int port) {
        this.ring = new ChordRing(4);
        this.hostname = hostname;
        this.port = port;
    }

    public int compareTo(ChordNode other) {
        return Integer.compare(id, other.getId());
    }

    public void newNetwork() throws RemoteException {
        id          = ring.random();
        successor   = this;
        predecessor = this;
    }

    public ChordNode lookup(int k) throws RemoteException {
        if (ring.between(k, predecessor.getId(), id)) {
            return this;
        }

        return successor.lookup(k);
    }

    public int getId() throws RemoteException {
        return id;
    }

    /**
     * @param firstNode the first ChordNode this ChordNode knows about
     */
    public void join(ChordNode firstNode) throws RemoteException {
        int potentialId         = ring.random();
        ChordNode potentialSucc = firstNode.lookup(potentialId);

        // Try and get an unused ID
        while (potentialSucc.getId() == potentialId) { // Should stop when all taken.
            potentialId   = ring.random();
            potentialSucc = firstNode.lookup(potentialId);
        }
        id = potentialId;

        // Put ourself in the chain
        successor   = potentialSucc;
        predecessor = successor.getPredecessor();
        predecessor.setSuccessor(this);
        successor.setPredecessor(this);
    }

    public void leave() throws RemoteException {
        this.predecessor.setSuccessor(this.successor);
        this.successor.setPredecessor(this.predecessor);
        return;
    }

    public int getID() throws RemoteException {
        return this.id;
    }

    public void setSuccessor(ChordNode succ) throws RemoteException {
        this.successor = succ;

    }

    public void setPredecessor(ChordNode pred) throws RemoteException {
        this.predecessor = pred;
    }

    public ChordNode getSuccessor() throws RemoteException {
        return successor;

    }

    public ChordNode getPredecessor() throws RemoteException {
        return predecessor;
    }

    @Override
    public String toString() {
        return String.format("PredID: %2d\nID:     %2d\nSuccID: %2d",
                   predecessor.getId(), id, successor.getId());
    }
}
