package googol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Barrel extends UnicastRemoteObject implements Barrel_int, Serializable {
    ConcurrentHashMap <String, Set <String>> processed;
    ConcurrentHashMap <String, Set <String>> reachable;
    ConcurrentHashMap <String, ArrayList <String>> elements; 
    ConcurrentHashMap <String, Integer> wordCount; // Para contar a quantas vezes cada palavra aparece
    static Set <String> stopWords; 

    public Barrel () throws RemoteException {
        super ();
        processed = new ConcurrentHashMap<>();
        reachable = new ConcurrentHashMap<>();
        elements = new ConcurrentHashMap<>();
        stopWords = new HashSet<>();
        wordCount = new ConcurrentHashMap<>();

    }

    public static void main(String[] args) {
        Boolean update = false;
        try {
            Barrel barrel1 = new Barrel();
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2])).lookup("googol");
            Barrel_int barrel;
            
            try {  
                Set <String> availableBarrels = gateway.getAvailableBarrels();
                if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");
                for (String ip_port: availableBarrels){
                    String [] ipport = ip_port.split (" ");
                    
                    try{
                        barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                        Barrel barrel2 = (Barrel) barrel.getUpdate();
                        barrel1.processed = barrel2.processed;
                        barrel1.reachable = barrel2.reachable;
                        barrel1.elements = barrel2.elements;
                        barrel1.wordCount = barrel2.wordCount;
                        System.out.println ("Barrel updated...");
                        update = true;
                        break;

                    } catch (Exception e){
                        System.err.println("Failed to update from barrel at " + ip_port);

                    }
                }
            } catch (Exception e) {
                System.err.println("No barrels available to update from");
            }

            if(!update){
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("resources/info.obj"))) {
                    System.out.println("Looking for backup...");
                    Barrel object = (Barrel) ois.readObject();
                    barrel1.processed = object.processed;
                    barrel1.reachable = object.reachable;
                    barrel1.elements = object.elements;
                    barrel1.wordCount = object.wordCount;
                    System.out.println("Backup loaded successfully.");
                
                } catch (FileNotFoundException e) {
                    System.err.println("Backup file not found.");

                } catch (IOException e) {
                    System.err.println("Error reading the backup file.");
                    e.printStackTrace();

                } catch (ClassNotFoundException e) {
                    System.err.println("Backup file format is incorrect.");
                    e.printStackTrace();
                }
            }

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind ("barrel", barrel1);

            System.out.println("Barrel is now operational");
            
            InetAddress ip = InetAddress.getLocalHost();
            String ipString = ip.getHostAddress();
            String ip_port = ipString + " " + args[0];
            gateway.addBarrel(ip_port);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                System.out.println("Shutdown hook triggered. Saving Data, might take some time...");
                try {
                    for (String word: stopWords){
                        System.out.println (word);

                    }
                                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                String filename = "resources/info.obj";
                File file = new File(filename);
                File parentDir = file.getParentFile();

                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();  // Criar o diretório pai se não existir
                }

                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
                    out.writeObject(barrel1);
                    System.out.println("Saving barrel data in " + filename);
                    gateway.getAvailableBarrels().remove(ip_port);
                    System.out.println("Barrel removed from the list of available barrels.");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }));


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
                
                if(results == null){
                    results = new HashSet<>(found);
                }

                else{
                    results.retainAll(found);
                }

            }
            
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
                
            }

            ConcurrentHashMap <String,Integer> filtered = new ConcurrentHashMap<>();

            for (String result: results){
                Integer num = reachable.get(result).size();
                filtered.put (result,num);
            }
            
            List<String> sortedUrls = filtered.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) 
                .map(Map.Entry::getKey) // apenas obtém os URLs
                .toList();

            return sortedUrls;
        }
    }

    public void addToIndex (ArrayList<String> words, Set<String> links, ArrayList <String> pageElems, String url) throws RemoteException {
        synchronized (processed) {
            synchronized (wordCount) {

                List<Integer> frequencies = wordCount.values()
                .stream()
                .sorted()
                .toList();

                if (wordCount != null || !wordCount.isEmpty()){
                    int size = frequencies.size();
                    int q1 = frequencies.get(size / 4);
                    int q3 = frequencies.get(3 * size / 4);
                    int iqr = q3 - q1;
                    int upperBound = q3 + (int) Math.round(2.0 * iqr);

                    for (String word: words){
                        wordCount.merge(word, 1, Integer::sum);
                        if (wordCount.get(word) <= upperBound) {
                            processed.computeIfAbsent(word, k -> new HashSet<>()).add(url);
                            if (stopWords.contains (word)){
                                stopWords.remove(word);
                            }
                        }

                        else {
                            stopWords.add (word);
                        }
                    }
                }

            synchronized (reachable) {
                for (String link: links){
                    reachable.computeIfAbsent(link, k -> new HashSet<>()).add(url);
                }
            }

            synchronized (elements) {
                elements.computeIfAbsent(url, k -> new ArrayList<>()).addAll(pageElems);
            }
        }
    }
}

    public Set <String> getReachableUrls (String url) throws RemoteException {
        return reachable.get (url);
    }

    public Barrel_int getUpdate () throws RemoteException{
        return this;
    }

}
