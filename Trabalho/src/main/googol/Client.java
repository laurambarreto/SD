package googol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.Set;

public class Client{
    public static void main (String [] args) throws NotBoundException, IOException{
        Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("googol");

        InputStreamReader in = new InputStreamReader (System.in);
        BufferedReader reader = new BufferedReader (in);

        System.out.println("Server ready. Waiting for input...");
        try {
            gateway.indexUrl("https://pt.wikipedia.org/wiki/");
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
        while (true){
            menu ();
            Integer num = Integer.parseInt(reader.readLine ());

            switch (num) {
                case 1:
                    try {
                        System.out.println ();
                        System.out.print ("Search: ");
                        String input = reader.readLine();
                        String [] line = input.split (" ");
        
                        if (line.length == 0){
                            System.out.println ();
                            System.out.println("Empty input, please enter a valid word!");
                        }

                        Set <String> results = gateway.search (line);
                        
                        if (results == null) {
                            System.out.println ();
                            System.out.println ("No results found");
                            System.out.println ();
                            continue;
                        }
        
                        System.out.println ();
                        System.out.println ("Results found for " + String.join (" ", line));
                        for (String url : results) {
                            System.out.println (url);
                        }
                        System.out.println ();
                       
        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    System.out.print ("Insert Url: ");
                    String url = reader.readLine ();
                    gateway.indexUrl(url);
                    break;
            }
        }
    }

    public static void menu () {
        System.out.println ("Choose what you want to do: ");
        System.out.println ("1 - Search");
        System.out.println ("2 - Add a new URL to process");
    }
}