package googol;
import java.rmi.*;
import java.util.*;

public interface Gateway_int extends Remote {
    public String takeNext () throws RemoteException;
    public void indexUrl (String newUrl) throws RemoteException;
    public Set <String> search (String [] line) throws RemoteException;
    public void addToIndex (String word, String url) throws RemoteException;
}
