package googol;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface Gateway_int extends Remote {
    public String takeNext () throws RemoteException;
    public void indexUrl (String newUrl) throws RemoteException;
    public List <String> search (String [] line) throws RemoteException;
    public void addBarrel (String ip_port) throws RemoteException;
    public Set<String> getAvailableBarrels () throws RemoteException;
    public Set <String> getReachableUrls (String url) throws RemoteException;
    

}
