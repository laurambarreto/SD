package googol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Client{
    public static void main (String [] args) throws NotBoundException, IOException{
        Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("googol");

        InputStreamReader in = new InputStreamReader (System.in);
        BufferedReader reader = new BufferedReader (in);

        System.out.println("Server ready. Waiting for input...");
        try {
            Set <String> urls = new HashSet<>();
            urls.add ("https://pt.wikipedia.org/wiki/");
            gateway.indexUrl(urls);

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

                        List <String> results = gateway.search (line);
                        
                        if (results == null || results.isEmpty()) {
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
                    String url1 = reader.readLine ();
                    Set <String> urls = new HashSet<>();
                    urls.add (url1);
                    gateway.indexUrl(urls);
                    break;

                case 3:
                    System.out.print ("Insert Url: ");
                    String url2 = reader.readLine ();
                    System.out.println ();
                    System.out.println ("The inserted url is available in: ");
                    System.out.println ();
                    for (String url: gateway.getReachableUrls(url2)){
                        System.out.println (url);
                    }
                    break;

            }
        }
    }

    public static void menu () {
        System.out.println ();
        System.out.println ("Choose what you want to do: ");
        System.out.println ("1 - Search");
        System.out.println ("2 - Add a new URL to process");
        System.out.println ("3 - Discover pages that contain Url");
    }
}