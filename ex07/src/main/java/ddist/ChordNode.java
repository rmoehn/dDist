package ddist;

import ddist.ChordRing;
import java.util.Comparator;
import ddist.IChordNode;
import java.rmi.RemoteException;

public class ChordNode implements IChordNode {
    private ChordNode successor;
    private ChordNode predecessor;
    private final ChordRing ring;
    private int id;

    public ChordNode(int ringSize) {
        this.ring = new ChordRing(ringSize);
    }

    public void newNetwork() throws RemoteException {
        id          = ring.random();
        successor   = this;
        predecessor = this;
    }

    /**
     * @param firstNode the first ChordNode this ChordNode knows about
     */
    public void join(ChordNode firstNode) throws RemoteException {
        int potentialId         = ring.random();
        ChordNode potentialSucc = firstNode.lookup(potentialId);

        // Try and get an unused ID
        while (potentialSucc.getID() == potentialId) { // Should stop when all taken.
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

    public ChordNode lookup(int k) throws RemoteException {
        if (ring.between(k, predecessor.getID(), id)) {
            return this;
        }

        return successor.lookup(k);
    }

    public void setSuccessor(ChordNode succ) throws RemoteException {
        this.successor = succ;

    }

    public void setPredecessor(ChordNode pred) throws RemoteException {
        this.predecessor = pred;
    }

    public int getID() throws RemoteException {
        return this.id;
    }

    public ChordNode getSuccessor() throws RemoteException {
        return successor;

    }

    public ChordNode getPredecessor() throws RemoteException {
        return predecessor;
    }

    @Override
    public String toString() {
        String ret = "";
        try {
        ret = String.format("PredID: %2d\nID:     %2d\nSuccID: %2d",
                             predecessor.getID(), id, successor.getID());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ret;
    }

 //    public int compareTo(ChordNode other) {
//         return Integer.compare(id, other.getID());
//     }
}
