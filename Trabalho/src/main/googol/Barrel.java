package googol;

import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Barrel extends UnicastRemoteObject implements Barrel_int {
    ConcurrentHashMap <String, Set <String>> processed;

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

    public Set <String> search (String [] line) throws RemoteException {
        try {
            synchronized (processed){
                Set<String> finalResults = new HashSet<>();
                Set<String> results = processed.get(line[0]);
                
                for (String word : line) {
                    results.retainAll(processed.get(word));
                }
                
                if (finalResults.isEmpty()) {
                    return null;
                    
                } else {
                    return finalResults;
                }
            }
                
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void addToIndex(String word, String url) throws RemoteException {
        processed.computeIfAbsent(word, k -> new HashSet<>()).add(url);
        
    }

}
