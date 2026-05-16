package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.UserResponseDto;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
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

@Component
public class OpenAIDebateGenerationClient implements DebateGenerationClient {
    @Value("${openai.key}")
    String secret_key;

    @Value("classpath:openai/prompts/argument-prompt.txt")
    private Resource argumentPromptResource;

    @Value("classpath:openai/schemas/argument-schema.json")
    private Resource argumentSchemaResource;

    @Value("classpath:openai/prompts/debate-prompt.txt")
    private Resource debatePromptResource;

    @Value("classpath:openai/schemas/debate-schema.json")
    private Resource debateSchemaResource;

    public AIDebateResponse generateDebate(String thesis) throws APIkeyNotFoundException, ServiceNotRespondingException {
        if(secret_key == null || secret_key.isEmpty()){
            throw new APIkeyNotFoundException("OpenAI Api's key was not found");
        }
        String schema = loadResourceToString(debateSchemaResource);
        String prompt = createDebatePrompt(thesis);
        String requestBody = createRequestBody(prompt, schema);
        HttpResponse<String> response = sendPostRequest(requestBody);
        return parseDebateResponse(response, thesis);
    }

    public String generateArgument(String type, String argumentText, String debate) throws APIkeyNotFoundException, ServiceNotRespondingException {
        if(secret_key == null || secret_key.isEmpty()){
            throw new APIkeyNotFoundException("OpenAI Api's key was not found");
        }

        String schema = loadResourceToString(argumentSchemaResource);
        String prompt = createArgumentPrompt(type, argumentText, debate);
        String requestBody = createRequestBody(prompt, schema);
        HttpResponse<String> response = sendPostRequest(requestBody);
        return parseArgumentResponse(response);
    }




    private String createArgumentPrompt(String type, String argumentText, String debate) throws ServiceNotRespondingException {
        String rewrittenType = type.equals("PRO") ? "supporting" : "opposing";
        String template = loadResourceToString(argumentPromptResource);
        return template.formatted(rewrittenType, argumentText, debate);
    }

    private String createDebatePrompt(String thesis) throws ServiceNotRespondingException {
        String template = loadResourceToString(debatePromptResource);
        return template.formatted(thesis);
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





    private AIDebateResponse parseDebateResponse(HttpResponse<String> response, String thesisText) {
        JSONObject json = new JSONObject(response.body());

        JSONArray outputArray = json.getJSONArray("output");
        JSONObject firstOutput = outputArray.getJSONObject(0);
        JSONArray content = firstOutput.getJSONArray("content");
        JSONObject firstContent = content.getJSONObject(0);

        String dataString = firstContent.getString("text");
        JSONObject debateData = new JSONObject(dataString);

        AIDebateResponse finalResponse = new AIDebateResponse();
        finalResponse.setThesis(thesisText);

        UserResponseDto virtualUser = new UserResponseDto();
        virtualUser.setId(0L);
        virtualUser.setUsername("AI");
        finalResponse.setOwner(virtualUser);

        long currentId = 1;
        long thesisId = 1;

        ArgumentResponseDto thesis = new ArgumentResponseDto();
        thesis.setId(currentId++);
        thesis.setText(thesisText);
        thesis.setType("THESIS");
        thesis.setParent(null);
        thesis.setOwner(virtualUser);
        finalResponse.getArguments().add(thesis);

        JSONArray proArgs = debateData.getJSONArray("pro");
        for (int i = 0; i < proArgs.length(); i++) {
            finalResponse.getArguments().add(createArgument(
                    currentId++,
                    proArgs.getString(i),
                    "PRO",
                    thesisId,
                    virtualUser
            ));
        }

        JSONArray conArgs = debateData.getJSONArray("con");
        for (int i = 0; i < conArgs.length(); i++) {
            finalResponse.getArguments().add(createArgument(
                    currentId++,
                    conArgs.getString(i),
                    "CON",
                    thesisId,
                    virtualUser
            ));
        }

        return finalResponse;
    }

    private String parseArgumentResponse(HttpResponse<String> response) throws ServiceNotRespondingException {
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
        JSONObject argumentData = new JSONObject(dataString);
        return argumentData.getString("argument");
    }

    private ArgumentResponseDto createArgument(Long id, String text, String type, Long parentId, UserResponseDto owner) {
        ArgumentResponseDto arg = new ArgumentResponseDto();
        arg.setId(id);
        arg.setText(text);
        arg.setType(type);
        arg.setParent(parentId);
        arg.setOwner(owner);
        return arg;
    }





    private String loadResourceToString(Resource resource) throws ServiceNotRespondingException {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceNotRespondingException("Failed to load template resource: " + resource.getFilename(), e);
        }
    }
}