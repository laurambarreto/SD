package googol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface Barrel_int extends Remote{
    public List <String> search (String [] line) throws RemoteException;
    public void addToIndex (ArrayList <String> words, Set<String> links,ArrayList <String> elems, String url) throws RemoteException;
    public Set <String> getReachableUrls (String url) throws RemoteException;
    public ArrayList <Object> getUpdate () throws RemoteException;
    
}
