package ddist;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChordNode extends Remote {
    
    public void newNetwork() throws RemoteException;

    /**
     * @param firstNode the first ChordNode this ChordNode knows about
     */
    public void join(ChordNode firstNode) throws RemoteException;

    public void leave() throws RemoteException;

    public ChordNode lookup(int k) throws RemoteException;

    public void setSuccessor(ChordNode succ) throws RemoteException;

    public void setPredecessor(ChordNode pred) throws RemoteException;

    public int getID() throws RemoteException;

    public ChordNode getSuccessor() throws RemoteException;

    public ChordNode getPredecessor() throws RemoteException;


}
