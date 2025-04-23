package googol;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Gateway extends UnicastRemoteObject implements Gateway_int {
    BlockingQueue <String> toBeProcessed;
    Set <String> availableBarrels;

    public Gateway () throws RemoteException {
        super ();
        toBeProcessed = new LinkedBlockingQueue <String> ();
        availableBarrels = new HashSet<>();
    
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
<<<<<<< HEAD
                
                toBeProcessed.put(url);
=======
                if (!queued.contains(url)){
                    toBeProcessed.put(url);
                }
>>>>>>> 6f50c27ffd4c692c1f06b87de9d6e6c694f6d145
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException ("Interrupted while waiting to put new URL", e);
        }

    }

    public List <String> search (String [] line) throws RemoteException {
        Barrel_int barrel;
        if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");

        for (String ip_port: availableBarrels){
            String [] ipport = ip_port.split (" ");
            
            try{
                barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                List <String> results = barrel.search (line);
                System.out.println("You're connected to a barrel!");
                return results;

            } catch (Exception e) {
                System.err.println("Failed to connect to barrel at " + ip_port + ", removing from list...");
                availableBarrels.remove(ip_port);
                System.out.println();
                System.out.println("Removed barrel from the list: " + ip_port);
            }
        }
        throw new RemoteException ("Waiting for a barrel to connect...");

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

    public Set <String> getAvailableBarrels () throws RemoteException{
        
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