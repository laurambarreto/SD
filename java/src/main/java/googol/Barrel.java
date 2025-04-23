package googol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Barrel extends UnicastRemoteObject implements Barrel_int, Serializable {
    ConcurrentHashMap <String, Set <String>> processed;
    ConcurrentHashMap <String, Set <String>> reachable = new ConcurrentHashMap<>();

    public Barrel () throws RemoteException {
        super ();
        processed = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        Boolean update = false;
        try {
            Barrel server = new Barrel();
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2])).lookup("googol");
            Barrel_int barrel;
            
            try {  
                Set <String> availableBarrels = gateway.getAvailableBarrels();
                if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");
                for (String ip_port: availableBarrels){
                    String [] ipport = ip_port.split (" ");
                    
                    try{
                        barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                        Barrel temp = (Barrel) barrel.getUpdate();
                        server.processed = temp.processed;
                        server.reachable = temp.reachable;
                        update = true;
                        break;

                    } catch (Exception e){
                        System.err.println("No Updates");

                    }
                }
            } catch (Exception e) {
                System.err.println("No Updates");
            }

            if(update == false){
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("../resources/barrel1.obj"))) {
                    System.err.println("Looking for backup");
                    Barrel object = (Barrel)ois.readObject(); // Read the object from the binary file
                    server.processed = object.processed;
                    server.reachable = object.reachable;
                
                }catch (Exception e) {
                    System.err.println("No backup found");
                }
            }

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind ("barrel", server);

            System.err.println("Barrel is now operational");
            
            InetAddress ip = InetAddress.getLocalHost();
            String ipString = args[3];//ip.getHostAddress(); //bugged in windows 11
            System.out.println(ipString);
            String ip_port = ipString + " " + args[0];
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                System.out.println("Shutdown hook triggered. Saving Data, might take some time...");
                
                String filename = "resources/barrel1.obj";
                File file = new File(filename);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();  // Create the directory structure
                }
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
                    out.writeObject(server);
                    System.out.println("Saving barrel in " + filename);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }));
            
            gateway.addBarrel(ip_port);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }        

    public List <String> search (String [] line) throws RemoteException {
        synchronized (processed){
            Set<String> results = null; 
            Set<String> found;
            for (String word : line){
                found = processed.get(word);

                if(found == null || found.isEmpty()){
                    return Collections.emptyList();
                } 
                
                if(results == null || results.isEmpty()){
                    results = found;
                }

                else{
                    results.retainAll(found);
                }

            }
            
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
                
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
    }

    public void addToIndex (Set<String> words, Set<String> links, String url) throws RemoteException {
        boolean stopWord = false;
        synchronized (processed) {
            for (String word: words){
                processed.computeIfAbsent(word, k -> new HashSet<>()).add (url);
                //if (processed.get (word).size () > xnum) {
                    //stopWord = true;
                    //blocks the stop word 
                //} we tried to implement the stop words but we didn't have time
            }
        }

        for (String link: links){
            reachable.computeIfAbsent(link, k -> new HashSet<>()).add(url);
        }
        
    }

    public Set <String> getReachableUrls (String url){
        return reachable.get (url);
    }

    public UnicastRemoteObject getUpdate () throws RemoteException{
        return this;
    }

}
