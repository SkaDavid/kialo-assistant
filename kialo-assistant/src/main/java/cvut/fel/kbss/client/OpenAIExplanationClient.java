package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.ValidationResponse;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.ServiceNotRespondingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Component
public class OpenAIExplanationClient implements ExplanationClient{
    @Value("${openai.key}")
    String secret_key;


    @Override
    public ValidationResponse explainFallacy(String label, String text) throws APIkeyNotFoundException, ServiceNotRespondingException {
        if(secret_key == null || secret_key.isEmpty()){
            throw new APIkeyNotFoundException("OpenAI Api's key was not found");
        }

        String schema = createSchema();
        String prompt = createPrompt(label, text);
        String requestBody = createRequestBody(prompt, schema);
        HttpResponse<String> response = sendPostRequest(requestBody);

        return parseFallacyResponse(response);
    }

    private ValidationResponse parseFallacyResponse(HttpResponse<String> response) throws ServiceNotRespondingException {
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            throw new ServiceNotRespondingException("OpenAI returned status " + statusCode);
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray outputArray = json.getJSONArray("output");
        JSONObject firstOutput = outputArray.getJSONObject(0);
        JSONArray content = firstOutput.getJSONArray("content");
        JSONObject firstContent = content.getJSONObject(0);
        String dataString = firstContent.getString("text");
        JSONObject data = new JSONObject(dataString);

        ValidationResponse finalResponse = new ValidationResponse();
        finalResponse.setFallacy(data.getBoolean("is_fallacy"));
        finalResponse.setExplanation(data.getString("explanation"));

        return finalResponse;
    }

    private HttpResponse<String> sendPostRequest(String requestBody) throws ServiceNotRespondingException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + secret_key)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Connection to OpenAI refused", e);
        }
    }

    private String createRequestBody(String prompt, String schema) {
        JSONObject body = new JSONObject();
        body.put("model", "gpt-4.1");

        JSONArray input = new JSONArray();
        JSONObject inputContent = new JSONObject();
        inputContent.put("role", "user");
        inputContent.put("content", prompt);
        input.put(inputContent);

        body.put("input", input);
        body.put("temperature", 0.6);
        body.put("max_output_tokens", 500);

        JSONObject format = new JSONObject();
        format.put("type", "json_schema");
        format.put("name", "fallacy_schema");
        format.put("schema", new JSONObject(schema));

        JSONObject text = new JSONObject();
        text.put("format", format);
        body.put("text", text);

        return body.toString();
    }

    private String createPrompt(String label, String text) {
        return """
            A fallacy detector labeled this text: "%s" as a fallacy of this type: "%s".
            Determine whether the fallacy detector is correct and briefly explain why.
            Do not include any additional text or information.
            """.formatted(text, label);
    }

    private String createSchema(){
        return """
                {
                  "type": "object",
                  "required": ["is_fallacy", "explanation"],
                  "properties": {
                    "is_fallacy": {
                      "type": "boolean",
                      "description": "True, if text does contain a fallacy."
                    },
                    "explanation": {
                      "type": "string",
                      "description": "A brief and logical explanation of why it is a fallacy or why it is not a fallacy."
                    }
                  },
                  "additionalProperties": false
                }
                """;
    }
}
