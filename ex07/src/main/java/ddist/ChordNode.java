package ddist;

import ddist.ChordRing;
import java.util.Comparator;
import ddist.IChordNode;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ChordNode extends UnicastRemoteObject implements IChordNode {
    public static final long serialVersionUID = 327L;

    private IChordNode successor;
    private IChordNode predecessor;
    private ChordRing ring;
    private int id;

    public ChordNode() throws RemoteException {
        super();
    }

    public void newNetwork(int ringBitSize) throws RemoteException {
        this.ring = new ChordRing(ringBitSize);
        id          = ring.random();
        successor   = this;
        predecessor = this;
    }

    /**
     * @param firstNode the first ChordNode this ChordNode knows about
     */
    public void join(IChordNode firstNode) throws RemoteException {
        this.ring = new ChordRing(firstNode.getRingBitSize());
        int potentialId         = ring.random();
        IChordNode potentialSucc = firstNode.lookup(potentialId);

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

    public IChordNode lookup(int k) throws RemoteException {
        if (ring.between(k, predecessor.getID(), id)) {
            return this;
        }

        return successor.lookup(k);
    }

    public String ringToString() throws RemoteException {
        return ringToStringHelper(predecessor.getID());
    }

    public String ringToStringHelper(int firstID) throws RemoteException {
        if (firstID == this.id) {
            return "" + this.toString();
        }
        return "" + this.toString() + "\n" + successor.ringToStringHelper(firstID);
    }

    public void setSuccessor(IChordNode succ) throws RemoteException {
        this.successor = succ;

    }

    public void setPredecessor(IChordNode pred) throws RemoteException {
        this.predecessor = pred;
    }

    public int getID() throws RemoteException {
        return this.id;
    }

    public int getRingBitSize() throws RemoteException {
        return this.ring.getBitSize();
    }

    public IChordNode getSuccessor() throws RemoteException {
        return successor;

    }

    public IChordNode getPredecessor() throws RemoteException {
        return predecessor;
    }

    @Override
    public String toString() {
        String ret = "";
        try {
        ret = String.format("PredID: %4d   ID: %4d   SuccID: %4d",
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
