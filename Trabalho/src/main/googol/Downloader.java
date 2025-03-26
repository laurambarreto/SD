package googol;
import java.rmi.registry.*;
import java.util.*;
import javax.lang.model.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.concurrent.ConcurrentHashMap;

public class Downloader {
    private static ConcurrentHashMap <String, Set <String>> reachable = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        try {
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(8012).lookup("googol");

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
            String [] words = text.split ("\\s+"); //tokenizer!!!!

            for (String word : words) {
                gateway.addToIndex (word.toLowerCase(), url);
            }

            for (Element link: links) {
                String href = link.attr("abs:href");
                if (!href.isEmpty()) {
                    reachable.computeIfAbsent(url, k -> new HashSet<>()).add(href);
                    gateway.indexUrl (href);
                }

            }
                //Todo: Read JSOUP documentation and parse the html to index the keywords. 
            //Then send back to server via index.addToIndex(...)
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
