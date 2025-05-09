package googol;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
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
            } catch (Exception e) { // se não conseguir obter o conteúdo da página, imprime mensagem 
                System.out.println ("Error obtaining page content: " + url);
                return;
            }

            // Obter todos os links da página
            Elements elements = doc.select("a[href]");

            // ArrayList para guardar os elementos da página em processamento
            ArrayList <String> pageElems = new ArrayList<>(); 

            // Obter o título da página e adicionar aos elementos
            String title = doc.title(); 
            pageElems.add (title); 

            // Obter uma citação da página e adicionar aos elementos
            String quote = doc.body().text().substring (0,50);            
            pageElems.add (quote); 

            StringTokenizer st = new StringTokenizer(doc.body().text(), " \t\n\r\f.,;:!?()[]{}<>\"'`~@#$%^&*-+=|\\/„“”’‘…—–_•°·");
            ArrayList<String> words = new ArrayList<>(); // adicionar todas as palavras encontradas a um Array 
            ConcurrentHashMap <String, Integer> wordCount = new ConcurrentHashMap <String, Integer>();
            while (st.hasMoreTokens()){ 
                String word = st.nextToken();
                words.add (word);
                if (wordCount.containsKey(word)){
                    wordCount.put(word, wordCount.get(word) + 1); // se a palavra já existe, incrementar o contador
                } else {
                    wordCount.put(word, 1); // se não existe, adicionar com contador 1
                }
            }

            Set <String> links = new HashSet<>();

            // Adicionar os links encontrados à lista de links
            for (Element link: elements){
                String href = link.attr("abs:href");

                if (!href.isEmpty() && !links.contains(href)){
                    links.add (href); // para cada link encontrado, adicionar ao Set dos Links
                }
            }

            // Verificar se existem barrels disponíveis
            Set<String> availableBarrels = gateway.getAvailableBarrels(); 
            Barrel_int barrel;

            // Se não houver barrels disponíveis, espera até que um esteja disponível
            if (availableBarrels == null || availableBarrels.isEmpty()){
               System.out.println ("No barrels available at the moment...");
            }  

            // Para os barrels encontrados, dividir a String que contém o IP e a porta
            for (String ip_port: availableBarrels){
                String [] ipport = ip_port.split (" ");
                
                // Conectar aos barrels e adicionar os dados processados
                try{
                    barrel = (Barrel_int)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                    barrel.addToIndex(wordCount, links, pageElems, url);
                    gateway.putUrl(links);

                } catch (Exception e){
                    System.out.println("Failed to send data to barrel at " + ip_port);
                    e.printStackTrace();

                }
            }
        
        } catch (Exception e) {
            System.out.println ("Error processing URL: " + url);
            e.printStackTrace();
        }
    }
}