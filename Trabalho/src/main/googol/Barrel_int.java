package googol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Barrel_int extends Remote{
    public void indexUrl (String newUrl) throws RemoteException;
    public Set <String> search (String [] line) throws RemoteException;
    public void addToIndex (String word, String url) throws RemoteException;
}
