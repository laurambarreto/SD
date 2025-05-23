package googol;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import jdk.jfr.MemoryAddress;

public class Gateway extends UnicastRemoteObject implements Gateway_int {
    BlockingQueue <String> toBeProcessed;
    BlockingQueue <String> availableBarrels;

    public Gateway () throws RemoteException {
        super ();
        toBeProcessed = new LinkedBlockingQueue <String> ();
        availableBarrels = new LinkedBlockingQueue <String>();
    
    }

    public static void main(String[] args) {
        try {
            Gateway server = new Gateway ();
            Registry registry = LocateRegistry.createRegistry (Integer.parseInt(args[0]));
            registry.rebind ("googol", server);
            System.out.println("Gateway ready on port " + args[0]);

        } catch (RemoteException e) {
            System.out.println ("Error creating server: " + e);
        }
    }        
    
    public String takeNext () throws RemoteException {
        String toProcess;
        try {
            toProcess = toBeProcessed.take (); // se a fila estiver vazia, espera até que um elemento esteja disponível

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException ("Interrupted while waiting for next task", e);
        }
        return toProcess;
    }

    public void putUrl (Set<String> newUrl) throws java.rmi.RemoteException{
        try {
            Set <String> queued = new HashSet<>(toBeProcessed);
            for (String url : newUrl){
                if (!queued.contains(url)){
                    toBeProcessed.put(url);
                }

            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException ("Interrupted while waiting to put new URL", e);
        }

    }

    public List <String> search (String [] line) throws RemoteException {
        Barrel_int barrel;
        if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");

        int attempts = availableBarrels.size();  // com dois barrels, será no máximo 2 tentativas

        while (attempts-- > 0) {
            String ip_port = availableBarrels.poll(); // tira o primeiroString ip_port = availableBarrels.poll(); // tira o primeiro
            String [] ipport = ip_port.split (" ");

            availableBarrels.remove(ip_port); // remove o barrel que não está a responder
            availableBarrels.add(ipport[0] + " " + ipport[1]); // adiciona o barrel que está a responder
            
            try{
                barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                List <String> results = barrel.search (line);
                System.out.println("You're connected to a barrel!");
                
                availableBarrels.offer(ip_port); // se funcionar, mete no fim
                
                availableBarrels.offer(ip_port); // se funcionar, mete no fim
                return results;

            } catch (Exception e) {
                System.err.println("Failed to connect to barrel at " + ip_port + ", removing from list...");
                availableBarrels.remove(ip_port);
                System.out.println();
                System.out.println("Removed barrel from the list: " + ip_port);
            }
         
        throw new RemoteException ("Waiting for a barrel to connect...");
        }
        return null; // não deve chegar aqui, mas é para evitar erro de compilação
        
    }
    public void indexHackerNewsUrls(String[] searchTerms) throws RemoteException {
        HackerNewsFetcher fetcher = new HackerNewsFetcher();
        List<String> stories = fetcher.fetchMatchingStories(searchTerms);
    
        if (stories.isEmpty()) {
            System.out.println("Nenhuma notícia encontrada com os termos fornecidos.");
            return;
        }
        putUrl(stories.stream().collect(Collectors.toSet()));
    }
    
    public void addBarrel (String ip_port) throws RemoteException{

        synchronized (availableBarrels) {
            try {
                String[] ipport = ip_port.split(" ");
                Barrel_int barrel = (Barrel_int) LocateRegistry.getRegistry(ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                availableBarrels.add(ip_port); 
                System.out.println("Added barrel: " + ip_port);
            } catch (Exception e) {
                System.err.println("Failed to connect to barrel at " + ip_port + ". It will not be added to the list.");
            }
        }
    }

    public BlockingQueue <String> getAvailableBarrels () throws RemoteException{
        
        synchronized (availableBarrels) {
            return availableBarrels;
        }
    }


    public Set <String> getReachableUrls (String url) throws RemoteException {
        Barrel_int barrel;
        if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");

        for (String ip_port: availableBarrels){
            String [] ipport = ip_port.split (" ");
            
            try{
                barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                Set <String> results = barrel.getReachableUrls (url);
                return results;

            } catch (Exception e){
                System.err.println("Failed to connect to barrel at " + ip_port + ", removing from list...");
                availableBarrels.remove(ip_port);
                System.out.println();
                System.out.println("Removed barrel from the list: " + ip_port);

            }
        }
        throw new RemoteException ("Waiting for a barrel to connect...");

    }

}