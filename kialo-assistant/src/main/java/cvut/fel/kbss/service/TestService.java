package cvut.fel.kbss.service;


import cvut.fel.kbss.utils.AI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestService {
    AI ai;
    @Autowired
    public TestService(AI ai){
        this.ai = ai;
    }

    public String getTest(){
        return "getTest called";
    }

    public String createDebate(String thesis){
        return ai.getResponse(thesis);
    }

    public String getTermitData(String label, String definition, String authorization) throws IOException, InterruptedException {
        String url = "http://termit-server:8080/termit/rest/vocabularies/testapivocabulary/terms";

        // Build payload exactly like Postman
        Map<String, Object> payload = new HashMap<>();
        payload.put("iri", "http://onto.fel.cvut.cz/ontologies/slovnik/nazev/pojem/" + label);
        payload.put("types", List.of("http://www.w3.org/2004/02/skos/core#Concept"));
        payload.put("label", Map.of("cs", label));
        payload.put("scopeNote", Map.of("cs", ""));
        payload.put("definition", Map.of("cs", definition));
        payload.put("parentTerms", List.of());
        payload.put("sources", List.of());
        payload.put("vocabulary", Map.of("iri", "http://onto.fel.cvut.cz/ontologies/slovnik/nazev"));
        payload.put("labelExist", Map.of("cs", false));

        Map<String, Object> context = new HashMap<>();
        context.put("label", Map.of("@id", "http://www.w3.org/2004/02/skos/core#prefLabel", "@container", "@language"));
        context.put("altLabels", Map.of("@id", "http://www.w3.org/2004/02/skos/core#altLabel", "@container", List.of("@language","@set")));
        context.put("hiddenLabels", Map.of("@id", "http://www.w3.org/2004/02/skos/core#hiddenLabel", "@container", List.of("@language","@set")));
        context.put("definition", Map.of("@id", "http://www.w3.org/2004/02/skos/core#definition", "@container", "@language"));
        context.put("scopeNote", Map.of("@id", "http://www.w3.org/2004/02/skos/core#scopeNote", "@container", "@language"));
        context.put("parentTerms", "http://www.w3.org/2004/02/skos/core#broader");
        context.put("exactMatchTerms", "http://www.w3.org/2004/02/skos/core#exactMatch");
        context.put("relatedTerms", "http://www.w3.org/2004/02/skos/core#related");
        context.put("relatedMatchTerms", "http://www.w3.org/2004/02/skos/core#relatedMatch");
        context.put("subTerms", "http://www.w3.org/2004/02/skos/core#narrower");
        context.put("sources", "http://purl.org/dc/terms/source");
        context.put("vocabulary", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/je-pojmem-ze-slovníku");
        context.put("definitionSource", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-zdroj-definice-termu");
        context.put("state", "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-stav-pojmu");
        context.put("glossary", "http://www.w3.org/2004/02/skos/core#inScheme");
        context.put("notations", "http://www.w3.org/2004/02/skos/core#notation");
        context.put("examples", Map.of("@id", "http://www.w3.org/2004/02/skos/core#example", "@container", List.of("@language","@set")));
        context.put("types", "@type");
        context.put("iri", "@id");
        context.put("term", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/je-přiřazením-termu");
        context.put("description", "http://purl.org/dc/terms/description");
        context.put("target", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-cíl");
        context.put("source", "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-zdroj");
        context.put("selectors", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-selektor");
        context.put("exactMatch", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-přesný-text-quote");
        context.put("prefix", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-prefix-text-quote");
        context.put("suffix", "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-suffix-text-quote");
        context.put("elementAbout", "http://purl.org/dc/terms/identifier");

        payload.put("@context", context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/ld+json"));
        headers.set("Authorization", authorization);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return "Response: " + response.getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
