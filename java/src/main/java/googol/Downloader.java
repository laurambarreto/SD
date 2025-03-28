package googol;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader {
    static Set <String> reachable = new HashSet<>();
    public static void main(String[] args) {
        try {
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("googol");

            while (true) {
                String url = gateway.takeNext ();
                if (url == null)
                    break;

                processUrl(url, gateway);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processUrl (String url, Gateway_int gateway){
        
        try {
            System.out.println ("Processing: " + url);
       
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            String title = doc.title();
            String text = doc.text();
            StringTokenizer st = new StringTokenizer(doc.body().text());
            HashSet<String> words = new HashSet<>();
            while (st.hasMoreTokens()){
                words.add(st.nextToken());
            }

            for (Element link: links){
                String href = link.attr ("href");

                if (!href.isEmpty()){
                    reachable.add (href);
                }
            }

            Set<String> availableBarrels = gateway.getAvailableBarrels ();
            Barrel_int barrel;
        if (availableBarrels == null || availableBarrels.isEmpty())  throw new RemoteException ("Waiting for a barrel to connect...");


        for (String ip_port: availableBarrels){
            String [] ipport = ip_port.split (" ");
            
            try{
                barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1]));
                barrel.addToIndex(words, reachable, url);


            } catch (Exception e){
                e.printStackTrace();

            }
        }


            //Todo: Read JSOUP documentation and parse the html to index the keywords. 
            //Then send back to server via index.addToIndex(...)
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}