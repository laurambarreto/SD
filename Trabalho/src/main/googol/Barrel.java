package googol;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Gatherer.Integrator;

public class Barrel extends UnicastRemoteObject implements Barrel_int {
    BlockingQueue <String> toBeProcessed;
    ConcurrentHashMap <String, Set <String>> processed;
    private Set <String> seenUrls;

    public Barrel () throws RemoteException {
        super ();
        toBeProcessed = new LinkedBlockingQueue <String> ();
        processed = new ConcurrentHashMap<>();
        seenUrls = ConcurrentHashMap.newKeySet();
    }

    public static void main(String[] args) {
        try {
            Gateway server = new Gateway ();
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind ("barrel", server);

        } catch (RemoteException e) {
            System.out.println ("Error creating server: " + e);
        }
    }        
    
    public String takeNext () throws RemoteException {
        String toProcess;
        try {
            toProcess = toBeProcessed.take (); // se a fila estiver vazia, bloqueia

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException ("Interrupted while waiting for next task", e);
        }
        return toProcess;
    }

    public void indexUrl (String newUrl) throws java.rmi.RemoteException{
        if (seenUrls.add(newUrl)) { // como é um set, se der para adicionar, tem de ser processado primeiro
            try {
                toBeProcessed.put (newUrl);
                seenUrls.add (newUrl);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RemoteException ("Interrupted while waiting to put new URL", e);
            }

        }
    }

    public Set <String> search (String [] line) throws RemoteException {
        try {
            synchronized (processed) {
                if (line.length == 0) {
                    return null;
                }
        
                Set<String> finalResults = new HashSet<>();
                Set<String> results = processed.get(line[0]);
        
                for (String url : results) {
                    boolean match = true;
        
                    for (int i = 1; i < line.length; i++) {
                        Set<String> wordResults = processed.get(line[i]);
                        if (wordResults == null || !wordResults.contains(url)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        finalResults.add(url);
                    }
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
