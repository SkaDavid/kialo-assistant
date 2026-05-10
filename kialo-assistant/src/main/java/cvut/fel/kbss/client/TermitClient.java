package cvut.fel.kbss.client;

import cvut.fel.kbss.exception.ServiceNotRespondingException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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

    public void createArgumentFile(String argumentText, long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String metadataBody = createFileBody(vocabIri, localName);
        String createUrl = "http://termit-server:8080/termit/rest/resources/document/files?namespace="
                + java.net.URLEncoder.encode(vocabIri + "/", java.nio.charset.StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(createUrl))
                    .header("Content-Type", "application/ld+json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(metadataBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Metadata Status: " + response.statusCode());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                String uploadUrl = "http://termit-server:8080/termit/rest/resources/" + localName + "/content?namespace="
                        + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

                String boundary = "JavaHttpClientBoundary" + System.currentTimeMillis();
                String htmlContent = "<html><body>" + argumentText + "</body></html>";
                
                String multipartBody = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + localName + "\"\r\n" +
                        "Content-Type: text/html\r\n\r\n" +
                        htmlContent + "\r\n" +
                        "--" + boundary + "--\r\n";

                HttpRequest uploadRequest = HttpRequest.newBuilder()
                        .uri(URI.create(uploadUrl))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.ofString(multipartBody))
                        .build();

                HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("Content Upload Status: " + uploadResponse.statusCode());
            }
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Chyba při nahrávání do Termitu", e);
        }
    }

    public String getArgumentContent(long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String url = "http://termit-server:8080/termit/rest/resources/" + localName + "/content?namespace="
                + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "text/html")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ServiceNotRespondingException("Termit error: " + response.statusCode() + " - " + response.body());
            }

            return response.body();
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Failed to fetch content from Termit", e);
        }
    }

    private String createFileBody(String vocabIri, String localName) {
        return """
                {
                    "iri": "%1$s/document/soubor/%2$s",
                    "types": [
                        "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/zdroj",
                        "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/soubor"
                    ],
                    "label": "%2$s",
                    "language": "en",
                    "@context": {
                        "label": "http://purl.org/dc/terms/title",
                        "iri": "@id",
                        "types": "@type"
                    }
                }
                """.formatted(vocabIri, localName);
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

    public String findDefinition(String resource, String token) throws ServiceNotRespondingException {
        String localName = resource.substring(resource.lastIndexOf("/") + 1);
        String namespace = resource.substring(0, resource.lastIndexOf("/") + 1);

        String url = "http://termit-server:8080/termit/rest/terms/" + localName
                + "?namespace=" + java.net.URLEncoder.encode(namespace, java.nio.charset.StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceNotRespondingException("Failed to fetch term definition from Termit");
            }
            JSONObject body = new JSONObject(response.body());
            return body.getJSONObject("definition").getString("en");
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Failed to fetch term definition from Termit", e);
        }
    }

    public void deleteArgumentFile(long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String fileName = debateId + "-" + argumentId + ".html";
        String resourceLocalName = "document";

        String fileNamespace = vocabIri + "/document/soubor/";

        String url = "http://termit-server:8080/termit/rest/resources/" + resourceLocalName
                + "/files/" + fileName
                + "?namespace=" + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                System.err.println("Smazání v Termitu selhalo: " + response.statusCode() + " - " + response.body());
                throw new ServiceNotRespondingException("Failed to delete file from Termit: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Connection error during file deletion", e);
        }
    }

    public void updateArgumentFile(long debateId, long argumentId, String newText, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String url = "http://termit-server:8080/termit/rest/resources/" + localName + "/content?namespace="
                + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newHttpClient()) {
            String boundary = "JavaHttpClientBoundary" + System.currentTimeMillis();
            String htmlContent = "<html><body>" + newText + "</body></html>";
            String multipartBody = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + localName + "\"\r\n" +
                    "Content-Type: text/html\r\n\r\n" +
                    htmlContent + "\r\n" +
                    "--" + boundary + "--\r\n";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(multipartBody))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new ServiceNotRespondingException("Failed to update content: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Error during Termit content update", e);
        }
    }
}