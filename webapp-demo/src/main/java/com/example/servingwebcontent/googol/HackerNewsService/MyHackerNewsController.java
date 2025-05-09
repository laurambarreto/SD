package com.example.servingwebcontent.googol.HackerNewsService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MyHackerNewsController {
    private static final Logger log = LoggerFactory.getLogger(MyHackerNewsController.class);

    // Quando alguém acede a http://localhost:8080/hackernewstopstories, este método é chamado
    @GetMapping("/hackernewstopstories")
    @ResponseBody
    public List<String> hackerNewsTopStories(@RequestParam(name = "search", required = false) String search) {
        // URL da API do Hacker News que devolve os IDs das top stories 
        String topStoriesEndpoint = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";

        // Cliente HTTP para fazer os pedidos REST
        RestTemplate restTemplate = new RestTemplate();
        List hackerNewsNewTopStories = restTemplate.getForObject(topStoriesEndpoint, List.class);

        // Confirmar que recebemos mesmo uma lista de histórias
        assert hackerNewsNewTopStories != null;
        log.info("Número total de top stories: " + hackerNewsNewTopStories.size());

        // Lista só com as histórias que tiverem todos os termos da pesquisa
        List <String> filteredTopStories = new ArrayList<>();
        
        // Vamos analisar só as 100 primeiras histórias para não sobrecarregar a API
        for (int i = 0; i < 100; i++) {
            Integer storyId = (Integer) hackerNewsNewTopStories.get(i);

            // Montamos o URL para ir buscar os detalhes de cada história individual
            String storyItemDetailsEndpoint = String.format("https://hacker-news.firebaseio.com/v0/item/%s.json?print=pretty", storyId);
            HackerNewsItemRecord hackerNewsItemRecord = restTemplate.getForObject(storyItemDetailsEndpoint, HackerNewsItemRecord.class);

            // Se por algum motivo não conseguirmos buscar os detalhes da história, saltamos para a próxima
            if (hackerNewsItemRecord == null) {
                log.error("Item " + storyId + " é nulo");
                continue;
            }

            log.info("Detalhes da história " + storyId + ": " + hackerNewsItemRecord);

            // Se foi fornecido um termo de pesquisa
            if (search != null) {
                log.info("Termo(s) de pesquisa: " + search);

                // Separar as palavras da pesquisa (todas em minúsculas para facilitar a comparação)
                List<String> searchTermsList = List.of(search.toLowerCase().split(" "));

                // Verificamos se todas as palavras da pesquisa existem no texto da notícia (também convertido para minúsculas)
                String text = hackerNewsItemRecord.text() != null ? hackerNewsItemRecord.text().toLowerCase() : "";
                boolean allTermsFound = searchTermsList.stream().allMatch(text::contains);

                // Só adicionamos a história à lista final se contiver todas as palvras da pesquisa
                if (allTermsFound) {
                    filteredTopStories.add(hackerNewsItemRecord.url());
                }
            } else {
                // Se não houver pesquisa, devolvemos todas as histórias
                System.out.println("Nenhum termo de pesquisa fornecido");
                filteredTopStories.add(hackerNewsItemRecord.url());
            }
        }

        // devolvemos a lista filtrada de histórias para a gateway indexar nos toBeProcessed
        return filteredTopStories;
    }
}
