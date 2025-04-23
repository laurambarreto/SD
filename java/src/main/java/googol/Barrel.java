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
<<<<<<< HEAD
    ConcurrentHashMap <String, Set <String>> reachable = new ConcurrentHashMap<>();

=======
    ConcurrentHashMap <String, Set <String>> reachable;
    ConcurrentHashMap <String, ArrayList <String>> elements; 
    ConcurrentHashMap <String, Integer> wordCount; // Para contar a quantas vezes cada palavra aparece
    
>>>>>>> 0a6ae6034af4fd28aba67bfeb5dce13c00fd1742
    public Barrel () throws RemoteException {
        super ();
        processed = new ConcurrentHashMap<>();
        reachable = new ConcurrentHashMap<>();
        elements = new ConcurrentHashMap<>();
        wordCount = new ConcurrentHashMap<>();

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
                        server.elements = temp.elements;
                        server.wordCount = temp.wordCount;
                        System.out.println ("Barrel updated...");
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
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("resources/barrel1.obj"))) {
                    System.out.println("Looking for backup...");
                    Barrel object = (Barrel) ois.readObject();
                    server.processed = object.processed;
                    server.reachable = object.reachable;
                    server.elements = object.elements;
                    server.wordCount = object.wordCount;
                    System.out.println("Backup loaded successfully.");

                    for (String key : server.processed.keySet()) {
                        System.out.println("Key: " + key);
                        System.out.println("Value: " + server.processed.get(key));
                    }

                    for (String key : server.reachable.keySet()) {
                        System.out.println("Key: " + key);
                        System.out.println("Value: " + server.reachable.get(key));
                    }
                
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
                    parentDir.mkdirs();  // Criar o diretório pai se não existir
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
                    results = new HashSet<>(found);
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
                    .map(Map.Entry::getKey) // apenas obtém os URLs
                    .toList();

                return sortedUrls;

            }
        }
    }

    public void addToIndex (ArrayList<String> words, Set<String> links, ArrayList <String> elems, String url) throws RemoteException {
        
        synchronized (processed) {

            synchronized (wordCount) {
                Set<String> stopWords = new HashSet<>();

                for (String word: words){
                    wordCount.merge(word, 1, Integer::sum);
                }

                List<String> sortedWords = wordCount.entrySet()
                    .stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) 
                    .map(Map.Entry::getKey)
                    .toList(); 

                sortedWords = sortedWords.subList(0, sortedWords.size()/10000);// a dividir por quartil  

                stopWords.addAll(sortedWords);
                for (String word: words){
                    if (!stopWords.contains(word)){
                        processed.computeIfAbsent(word, k -> new HashSet<>()).add (url);
                    
                    }
                }
            }

            synchronized (reachable) {
                for (String link: links){
                    reachable.computeIfAbsent(link, k -> new HashSet<>()).add(url);
                }
            }

            synchronized (elements) {
                elements.computeIfAbsent(url, k -> new ArrayList<>()).addAll(elems);
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
