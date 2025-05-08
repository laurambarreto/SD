package googol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Barrel_int extends Remote{
    public List <String> search (String [] line) throws RemoteException;
    public void addToIndex (ConcurrentHashMap <String, Integer> wordCount, Set<String> links,ArrayList <String> elems, String url) throws RemoteException;
    public Set <String> getReachableUrls (String url) throws RemoteException;
    public ArrayList <Object> getUpdate () throws RemoteException;
    
}
