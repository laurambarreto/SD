package googol;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    static Set <String> links = new HashSet<>();
    public static void main(String[] args) {
        try {
            Gateway_int gateway = (Gateway_int) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("googol");

            // Enquanto forem encontrados urls na queue da gateway, são processados
            while (true) { 
                String url = gateway.takeNext ();
                if (url == null)
                    break;

                // Processa o url retirado da queue
                processUrl(url, gateway);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processUrl (String url, Gateway_int gateway){
        
        try {
            Document doc;
            System.out.println ("Processing: " + url);

            // Tentar obter o conteúdo da página
            try {
                doc = Jsoup.connect(url).get();
            } catch (Exception e) { // se não tiver conteúdo, não devolve nada
                return;
            }

            // Obter todos os links da página
            Elements elements = doc.select("a[href]");

            // ArrayList para guardar os elementos da página em processamento
            ArrayList <String> elems = new ArrayList<>(); 

            // Obter o título da página e adicionar aos elementos
            String title = doc.title(); 
            elems.add (title); 

            // Obter uma citação da página e adicionar aos elementos
            String quote = doc.body().text().substring (0,100);            
            elems.add (quote); 

            StringTokenizer st = new StringTokenizer(doc.body().text());
            ArrayList<String> words = new ArrayList<>(); // adicionar todas as palavras encontradas a um Array 
            
            while (st.hasMoreTokens()){ 
                String word = st.nextToken();
                words.add (word);
                
            }

            // Adicionar os links encontrados à lista de links
            for (Element link: elements){
                String href = link.attr ("href"); 

                if (!href.isEmpty()){
                    links.add (href); // para cada link encontrado, adicionar ao Set dos Links
                }
            }

            // Verificar se existem barrels disponíveis
            Set<String> availableBarrels = gateway.getAvailableBarrels(); 
            Barrel_int barrel;

            // Se não houver barrels disponíveis, espera até que um esteja disponível
            if (availableBarrels == null || availableBarrels.isEmpty()){
               System.out.println ("Waiting for a barrel to connect...");
            }  

            // Para o barrel encontrado, dividir a String que contém o IP e a porta
            for (String ip_port: availableBarrels){
                String [] ipport = ip_port.split (" ");
                
                // Conectar ao barrel e adicionar os dados processados
                try{
                    barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                    Set <String> nonStopWords = barrel.stopWords(words);
                    barrel.addToIndex(nonStopWords, links, elems, url);
                    gateway.putUrl(links);

                } catch (Exception e){
                    e.printStackTrace();

                }
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}