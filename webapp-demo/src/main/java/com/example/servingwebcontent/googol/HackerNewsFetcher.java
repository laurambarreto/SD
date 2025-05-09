package googol;

import googol.HackerNewsService.HackerNewsItemRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


public class HackerNewsFetcher {

    public List<String> fetchMatchingStories(String[] searchTerms) {
        RestTemplate restTemplate = new RestTemplate();

        // Constrói o URL para o serviço Spring Boot (muda o localhost:8080 se for diferente)
        String baseUrl = "http://localhost:8080/hackernewstopstories";

        // Junta os termos de pesquisa com espaço
        String joinedTerms = String.join(" ", searchTerms);

        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("search", joinedTerms)
                .toUriString();

        // Faz o pedido e devolve a lista de objetos HackerNewsItemRecord
        HackerNewsItemRecord[] items = restTemplate.getForObject(uri, HackerNewsItemRecord[].class);

        // Extrai os URLs válidos
        List<String> urls = new ArrayList<>();
        if (items != null) {
            for (HackerNewsItemRecord item : items) {
                if (item.url() != null) {
                    urls.add(item.url());
                }
            }
        }
        return urls;
    }
}
