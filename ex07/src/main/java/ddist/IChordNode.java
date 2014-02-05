package ddist;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChordNode extends Remote {
    
    public void newNetwork(int ringBitSize) throws RemoteException;

    /**
     * @param firstNode the first ChordNode this ChordNode knows about
     */
    public void join(IChordNode firstNode) throws RemoteException;

    public void leave() throws RemoteException;

    public IChordNode lookup(int k) throws RemoteException;

    public String ringToString() throws RemoteException;

    public String ringToStringHelper(int firstID) throws RemoteException;

    public void setSuccessor(IChordNode succ) throws RemoteException;

    public void setPredecessor(IChordNode pred) throws RemoteException;

    public int getID() throws RemoteException;

    public int getRingBitSize() throws RemoteException;

    public IChordNode getSuccessor() throws RemoteException;

    public IChordNode getPredecessor() throws RemoteException;


}
