package googol;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Barrel extends UnicastRemoteObject implements Barrel_int {
    ConcurrentHashMap <String, Set <String>> processed;
    ConcurrentHashMap <String, Set <String>> reachable = new ConcurrentHashMap<>();
    
    public Barrel () throws RemoteException {
        super ();
        processed = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        try {
            Barrel server = new Barrel();
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind ("barrel", server);
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2])).lookup("googol");
            
            InetAddress ip = InetAddress.getLocalHost();
            String ipString = ip.getHostAddress();
            String ip_port = ipString + " " + args[0];
            gateway.addBarrel(ip_port);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }        

    public List <String> search (String [] line) throws RemoteException {
        try {
            synchronized (processed){
                Set<String> results = new HashSet<>();
                
                for (String word : line) {
                    results.retainAll(processed.get(word));
                }
                
                if (results.isEmpty()) {
                    return null;
                    
                } else {
                    ConcurrentHashMap <String,Integer> filtered = new ConcurrentHashMap<>();

                    for (String result: results){
                        Integer num = reachable.get(result).size();
                        filtered.put (result,num);
                    }
                    
                    List<String> sortedUrls = filtered.entrySet()
                        .stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) 
                        .map(Map.Entry::getKey) // Only obtains URLS
                        .toList();

                    return sortedUrls;

                }
            }
                
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void addToIndex (Set<String> words, Set<String> links, String url) throws RemoteException {
        boolean stopWord = false;
        synchronized (processed) {
            for (String word: words){
                processed.computeIfAbsent(word, k -> new HashSet<>()).add (url);
                if (processed.get (word).size () > xnum) {
                    stopWord = true;
                }
            }
        }

        for (String link: links){
            reachable.computeIfAbsent(link, k -> new HashSet<>()).add(url);
        }
        
    }

    public Set <String> getReachableUrls (String url){
        return reachable.get (url);
    }

}
