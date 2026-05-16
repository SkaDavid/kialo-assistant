package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.TermDefinitionDto;
import cvut.fel.kbss.exception.ServiceNotRespondingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class TermitClient {
    private static final String TERMIT_URL = "http://termit-server:8080/termit/rest";

    @Value("classpath:termit/dictionary-body.jsonld")
    private Resource dictionaryBodyResource;

    @Value("classpath:termit/file-body.jsonld")
    private Resource fileBodyResource;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void createDictionary(String topic, long debateId, String token) throws ServiceNotRespondingException {
        String body = createDictionaryBody(topic, debateId);
        String url = TERMIT_URL + "/vocabularies";
        executeRequest(url, "POST", body, "application/ld+json", null, token);
    }

    public void createArgumentFile(String argumentText, long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String body = createFileBody(vocabIri, localName);
        String createUrl = TERMIT_URL + "/resources/document/files?namespace="
                + java.net.URLEncoder.encode(vocabIri + "/", java.nio.charset.StandardCharsets.UTF_8);

        HttpResponse<String> response = executeRequest(createUrl, "POST", body, "application/json+ld", null, token);
        if(response.statusCode() == 200 || response.statusCode() == 201){
            String uploadUrl = TERMIT_URL + "/resources/" + localName + "/content?namespace="
                    + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

            String boundary = "JavaHttpClientBoundary" + System.currentTimeMillis();
            String htmlContent = "<html><body>" + argumentText + "</body></html>";

            String multipartBody = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + localName + "\"\r\n" +
                    "Content-Type: text/html\r\n\r\n" +
                    htmlContent + "\r\n" +
                    "--" + boundary + "--\r\n";

            HttpResponse<String> uploadResponse = executeRequest(uploadUrl, "PUT", multipartBody, "multipart/form-data; boundary=" + boundary, null, token);
        }
    }

    public String getArgumentContent(long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String url = TERMIT_URL + "/resources/" + localName + "/content?namespace="
                + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

        HttpResponse<String> response = executeRequest(url, "GET", null, null, "text/html", token);
        return response.body();
    }

    public List<TermDefinitionDto> getVocabularyTerms(long debateId, String token) throws ServiceNotRespondingException {
        String localName = "debate-" + debateId;
        String url =  TERMIT_URL + "/vocabularies/" + localName + "/terms";

        HttpResponse<String> response = executeRequest(url, "GET", null, null, "application/json", token);

        JSONArray termsArray = new JSONArray(response.body());
        List<TermDefinitionDto> result = new ArrayList<>();

        for (int i = 0; i < termsArray.length(); i++) {
            JSONObject termJson = termsArray.getJSONObject(i);
            TermDefinitionDto dto = new TermDefinitionDto();

            if (termJson.has("definition")) {
                JSONObject defObj = termJson.optJSONObject("definition");
                if (defObj != null) {
                    dto.setDefinition(defObj.optString("en", "No definition"));
                } else {
                    dto.setDefinition(termJson.optString("definition", "No definition"));
                }
            }
            if (termJson.has("label")) {
                JSONObject labelObj = termJson.optJSONObject("label");
                if (labelObj != null) {
                    dto.setTerm(labelObj.optString("en", "No label"));
                } else {
                    dto.setTerm(termJson.optString("label", "No label"));
                }
            }
            result.add(dto);
        }
        return result;
    }

    public String getDefinition(String resource, String token) throws ServiceNotRespondingException {
        String localName = resource.substring(resource.lastIndexOf("/") + 1);
        String namespace = resource.substring(0, resource.lastIndexOf("/") + 1);

        String url = TERMIT_URL + "/terms/" + localName
                + "?namespace=" + java.net.URLEncoder.encode(namespace, java.nio.charset.StandardCharsets.UTF_8);

        HttpResponse<String> response = executeRequest(url, "GET", null, null, "application/json", token);
        return new JSONObject(response.body()).getJSONObject("definition").getString("en");
    }

    public void deleteArgumentFile(long debateId, long argumentId, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String fileName = debateId + "-" + argumentId + ".html";
        String resourceLocalName = "document";

        String fileNamespace = vocabIri + "/document/soubor/";

        String url = TERMIT_URL +"/resources/" + resourceLocalName
                + "/files/" + fileName
                + "?namespace=" + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);

        executeRequest(url, "DELETE", null, null, null, token);
    }

    public void updateArgumentFile(long debateId, long argumentId, String newText, String token) throws ServiceNotRespondingException {
        String vocabIri = "http://onto.fel.cvut.cz/ontologies/slovnik/debate-" + debateId;
        String localName = debateId + "-" + argumentId + ".html";
        String fileNamespace = vocabIri + "/document/soubor/";

        String url = TERMIT_URL + "/resources/" + localName + "/content?namespace="
                + java.net.URLEncoder.encode(fileNamespace, java.nio.charset.StandardCharsets.UTF_8);
        String boundary = "JavaHttpClientBoundary" + System.currentTimeMillis();
        String htmlContent = "<html><body>" + newText + "</body></html>";
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + localName + "\"\r\n" +
                "Content-Type: text/html\r\n\r\n" +
                htmlContent + "\r\n" +
                "--" + boundary + "--\r\n";

        executeRequest(url, "PUT", multipartBody, "multipart/form-data; boundary=" + boundary, null, token);
    }

    private HttpResponse<String> executeRequest(
            String url,
            String method,
            String body,
            String contentType,
            String acceptType,
            String token) throws ServiceNotRespondingException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(30));

            if (contentType != null) builder.header("Content-Type", contentType);
            if (acceptType != null) builder.header("Accept", acceptType);

            switch (method.toUpperCase()) {
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body));
                case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body));
                case "DELETE" -> builder.DELETE();
                case "GET" -> builder.GET();
                default -> builder.GET();
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ServiceNotRespondingException("Termit error: " + response.statusCode() + " - " + response.body());
            }
            return response;
        } catch (ServiceNotRespondingException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Connection to Termit failed or refused", e);
        }
    }

    private String loadResourceToString(Resource resource) throws ServiceNotRespondingException {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceNotRespondingException("Failed to load template resource: " + resource.getFilename(), e);
        }
    }

    private String createFileBody(String vocabIri, String localName) throws ServiceNotRespondingException {
        String body = loadResourceToString(fileBodyResource);
        return body.formatted(vocabIri, localName);
    }

    private String createDictionaryBody(String topic, long debateId) throws ServiceNotRespondingException {
        String body = loadResourceToString(dictionaryBodyResource);
        return body.formatted(topic, debateId);
    }
}