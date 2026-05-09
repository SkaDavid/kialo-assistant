package cvut.fel.kbss.client;

import cvut.fel.kbss.exception.ServiceNotRespondingException;
import org.json.JSONObject;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class TermitClient {
    public void createDictionary(String topic, long debateId, String token) throws ServiceNotRespondingException {
        String body = createDictionaryBody(topic, debateId);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://termit-server:8080/termit/rest/vocabularies"))
                    .header("Content-Type", "application/ld+json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            System.out.println("Status Code: " + statusCode);


            String responseBody = response.body();
            System.out.println("Response Body: " + responseBody);

            response.headers().firstValue("Location").ifPresent(loc -> System.out.println("Created Resource at: " + loc));

        } catch (Exception e) {
            throw new ServiceNotRespondingException("Connection to Termit refused", e);
        }
    }

    private String createDictionaryBody(String topic, long debateId) {
        return """
                {
                     "iri": "http://onto.fel.cvut.cz/ontologies/slovnik/debate-%2$s",
                     "types": [
                         "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/slovník",
                         "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/dokumentový-slovník"
                     ],
                     "label": {
                         "en": "%1$s"
                     },
                     "comment": {
                         "en": "This dictionary was made to manage terms in debate: %1$s"
                     },
                     "document": {
                         "iri": "http://onto.fel.cvut.cz/ontologies/slovnik/debate-%2$s/document",
                         "types": [
                             "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/zdroj",
                             "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/dokument"
                         ],
                         "label": "Document for debate: %1$s",
                         "terms": [],
                         "files": [],
                         "@context": {
                             "label": "http://purl.org/dc/terms/title",
                             "description": "http://purl.org/dc/terms/description",
                             "iri": "@id",
                             "types": "@type",
                             "files": {
                                 "@id": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-soubor",
                                 "@container": "@set"
                             },
                             "vocabulary": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-dokumentový-slovník",
                             "content": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/soubor/content",
                             "owner": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/je-částí-dokumentu",
                             "language": "http://purl.org/dc/terms/language"
                         }
                     },
                     "primaryLanguage": "en",
                     "@context": {
                         "iri": "@id",
                         "types": "@type",
                         "label": {
                             "@id": "http://purl.org/dc/terms/title",
                             "@container": "@language"
                         },
                         "comment": {
                             "@id": "http://purl.org/dc/terms/description",
                             "@container": "@language"
                         },
                         "document": {
                             "@id": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/popisuje-dokument",
                             "@context": {
                                 "label": "http://purl.org/dc/terms/title",
                                 "description": "http://purl.org/dc/terms/description",
                                 "iri": "@id",
                                 "types": "@type",
                                 "files": {
                                     "@id": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-soubor",
                                     "@container": "@set"
                                 },
                                 "vocabulary": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-dokumentový-slovník",
                                 "content": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/soubor/content",
                                 "owner": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/je-částí-dokumentu",
                                 "language": "http://purl.org/dc/terms/language"
                             }
                         },
                         "glossary": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-glosář",
                         "model": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/má-model",
                         "importedVocabularies": "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/importuje-slovník",
                         "accessLevel": {
                             "@id": "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/má-úroveň-přístupových-oprávnění",
                             "@type": "@id"
                         },
                         "primaryLanguage": "http://purl.org/dc/terms/language"
                     }
                 }
                  """.formatted(topic, debateId);
    }


    public void createArgumentFile(String argumentText, long debateId, long argumentId, String token) throws ServiceNotRespondingException {

    }
}
