package googol;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public interface Gateway_int extends Remote {
    public String takeNext () throws RemoteException;
    public void putUrl (Set<String> newUrl) throws RemoteException;
    public List <String> search (String [] line) throws RemoteException;
    public void addBarrel (String ip_port) throws RemoteException;
    public BlockingQueue <String> getAvailableBarrels () throws RemoteException;
    public Set <String> getReachableUrls (String url) throws RemoteException;
    public void indexHackerNewsUrls (String[]searchTerms) throws RemoteException;
    
}
