package googol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import java.util.stream.Collectors;

public class Barrel extends UnicastRemoteObject implements Barrel_int, Serializable {
    ConcurrentHashMap <String, Set <String>> processed; // palavras já processadas e os links que as contêm
    ConcurrentHashMap <String, Set <String>> reachable; // links alcançados a partir de um determinado url
    ConcurrentHashMap <String, ArrayList <String>> elements; // elementos de cada página
    ConcurrentHashMap <String, Integer> globalWordCount; // Para contar a quantas vezes cada palavra aparece
    Set<String> stopWords; // palavras consideradas stop words (sem repetição)

    public Barrel () throws RemoteException {
        super ();
        processed = new ConcurrentHashMap<>();
        reachable = new ConcurrentHashMap<>();
        elements = new ConcurrentHashMap<>();
        globalWordCount = new ConcurrentHashMap<>();
        stopWords = new HashSet<>();

    }

    public static void main(String[] args) {
        Boolean update = false; // verificar se houve update
        try {
            Barrel barrel1 = new Barrel(); 
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[3], Integer.parseInt(args[2])).lookup("googol");
            Barrel_int barrel;
            
            try {  
                Set <String> availableBarrels = gateway.getAvailableBarrels();
                if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");
                for (String ip_port: availableBarrels){
                    String [] ipport = ip_port.split (" ");
                    
                    try {
                        barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                        ArrayList<Object> barrel2 = barrel.getUpdate();
                        barrel1.processed = (ConcurrentHashMap<String,Set<String>>)barrel2.get(0);
                        barrel1.reachable = (ConcurrentHashMap<String,Set<String>>)barrel2.get(1);
                        barrel1.elements = (ConcurrentHashMap<String,ArrayList<String>>)barrel2.get(2);
                        barrel1.globalWordCount = (ConcurrentHashMap<String,Integer>)barrel2.get(3);
                        barrel1.stopWords = (Set<String>)barrel2.get(4);
                        System.out.println ("Barrel updated...");
                        update = true;
                        break;

                    } catch (Exception e){
                        e.printStackTrace();
                        System.err.println("Failed to update from barrel at " + ip_port);

                    } catch(Exception e){
                        e.printStackTrace();
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
                    barrel1.globalWordCount = object.globalWordCount;
                    barrel1.stopWords = object.stopWords;
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
            
            String ipString = args [1];
            String ip_port = ipString + " " + args[0];
            gateway.addBarrel(ip_port);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                System.out.println("Shutdown hook triggered. Saving Data, might take some time...");
                
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
            synchronized (stopWords){
                Set<String> results = null; 
                Set<String> found;
                for (String word : line){
                    if (stopWords.contains(word)){
                        continue;
                    }

                    found = processed.get(word);

                    if(found == null || found.isEmpty()){
                        return Collections.emptyList();
                    } 
                    
                    if(results == null){
                        results = new HashSet<>(found);
                    }

                    else {
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
    }

    public void addToIndex (ConcurrentHashMap <String, Integer> wordCount, Set<String> links, ArrayList <String> pageElems, String url) throws RemoteException {
        synchronized (processed) {
            synchronized (globalWordCount) {
                synchronized (wordCount) {
                    if (wordCount != null || !wordCount.isEmpty()){
                        for (String word : wordCount.keySet()) {
                            if (globalWordCount.containsKey(word)) {
                                globalWordCount.put(word, globalWordCount.get(word) + wordCount.get(word));
                            } else {
                                globalWordCount.put(word, wordCount.get(word));
                            }
                        }
                    }

                    if (globalWordCount != null || !globalWordCount.isEmpty()){
                        List<Double> count = globalWordCount.values()
                            .stream()
                            .sorted()
                            .map(val -> val * 1.0) // transforma em Double
                            .collect(Collectors.toCollection(ArrayList::new));

                        double max = count.get(0);
                        double total = count.stream().mapToDouble(Double::doubleValue).sum();

                        for (int i = 0; i < count.size(); i++) {
                            count.set(i, count.get(i));
                        }

                        if (max >= 1000) {
                            double q1 = count.get (count.size() / 4);
                            double q3 = count.get (3 * count.size() / 4);
                            double iqr = q3 - q1;
                            double upperBound = q3 + 1.5 * iqr;
                            System.out.println("Upper Bound: " + upperBound);
                        
                            for (String word : globalWordCount.keySet()) {
                                double freq = globalWordCount.get(word);
    
                                if (freq <= upperBound) {
                                    processed.computeIfAbsent(word, k -> new HashSet<>()).add(url);
                                    if (stopWords.contains(word)) {
                                        stopWords.remove(word);
                                        System.out.println("Removing Stop word: " + word);
                                    }
                                } else {
                                    if (!stopWords.contains(word)) {
                                        stopWords.add(word);
                                        System.out.println("Adding Stop word: " + word);
                                    }
                                }
                            }
                            
                        } else {
                            // Se ainda não há dados suficientes, adiciona normalmente
                            for (String word : globalWordCount.keySet()) {
                                processed.computeIfAbsent(word, k -> new HashSet<>()).add(url);
                            }
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
    @Override
    public Set <String> getReachableUrls (String url) throws RemoteException {
        return reachable.get (url);
    }

    public ArrayList <Object> getUpdate () throws RemoteException{
        ArrayList <Object> update = new ArrayList<>();
        update.add(processed);
        update.add(reachable);
        update.add(elements);
        update.add(globalWordCount);
        update.add(stopWords);
        return update;
    }

}
