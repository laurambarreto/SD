package googol;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

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
        try {
            toBeProcessed.put (newUrl);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException ("Interrupted while waiting to put new URL", e);
        }

    }

    public List <String> search (String [] line) throws RemoteException {
        System.out.println("Looking for barrel...");
        Barrel_int barrel;
        if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");
        System.out.println("Barrels exist, but...");

        for (String ip_port: availableBarrels){
            String [] ipport = ip_port.split (" ");
            
            try{
                barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                System.out.println("Barrels, are you ok?");
                List <String> results = barrel.search (line);
                System.out.println("barrel!!!!");
                return results;

            } catch (Exception e){
                e.printStackTrace();

            }
        }
        throw new RemoteException ("Waiting for a barrel to connect...");

    }

    public void addBarrel (String ip_port) throws RemoteException{

        synchronized (availableBarrels){
            availableBarrels.add(ip_port);
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
                e.printStackTrace();

            }
        }
        throw new RemoteException ("Waiting for a barrel to connect...");

    }
    

}