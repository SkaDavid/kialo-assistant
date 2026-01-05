package cvut.fel.kbss.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class DebateService {
    @Value("${openai.key}")
    String secret_key;

    public String createDebate(String thesis) {
        // TODO vyjimky
        if (thesis == null || thesis.isEmpty()) {
            return "Thesis is empty";
        }
        if(secret_key == null || secret_key.isEmpty()){
            return "API key is not defined";
        }
        String requestBody = createRequestBody(thesis);
        HttpResponse<String> response = sendPostRequest(requestBody);
        return parseResponse(response);
    }

    private String parseResponse(HttpResponse<String> response) {
        JSONObject json = new JSONObject(response.body());
        System.out.println(response.body());
        JSONArray outputArray = json.getJSONArray("output");

        JSONObject firstOutput = outputArray.getJSONObject(0);
        JSONArray content = firstOutput.getJSONArray("content");
        JSONObject firstContent = content.getJSONObject(0);
        return firstContent.getString("text");
    }

    private HttpResponse<String> sendPostRequest(String requestBody){
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
            // TODO
            return null;
//            return "Error calling OpenAI API: " + e.getMessage();
        }
    }

    private String createRequestBody(String thesis) {
        String schema = createSchema();
        String prompt = createPrompt(thesis);

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4.1");

        JSONArray input = new JSONArray();
        JSONObject inputContent = new JSONObject();
        inputContent.put("role", "user");
        inputContent.put("content", prompt);
        input.put(inputContent);

        body.put("input", input);
        body.put("temperature", 0.4);
        body.put("max_output_tokens", 500);

        JSONObject format = new JSONObject();
        format.put("type", "json_schema");
        format.put("name", "debate_schema");
        format.put("schema", new JSONObject(schema));

        JSONObject text = new JSONObject();
        text.put("format", format);
        body.put("text", text);

        return body.toString();
    }

    private String createSchema() {
        return """
                {
                  "type": "object",
                  "required": ["pro", "con"],
                  "properties": {
                    "pro": {
                      "type": "array",
                      "minItems": 3,
                      "maxItems": 3,
                      "items":{
                        "type": "string"
                      }
                    },
                    "con": {
                      "type": "array",
                      "minItems": 3,
                      "maxItems": 3,
                      "items": {
                        "type": "string"
                      }
                    }
                  },
                  "additionalProperties": false
                }
                """;
    }

    private String createPrompt(String thesis){
        return """
         Create a debate of two supporting arguments and two opposing arguments regarding this topic: "%s".
         Return a JSON object with two parts:
         "pro": an array of two supporting arguments.
         "con": an array of two opposing arguments.
         An arguments is always a string.
         Do not include any additional text or explanation.
         """.formatted(thesis);
    }
}
