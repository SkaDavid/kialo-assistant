package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.request.FallacyRequestDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.exception.ServiceNotRespondingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BertFallacyClient implements FallacyClient {
    private final RestTemplate restTemplate;

    @Autowired
    public BertFallacyClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public FallacyResponseDto testFallacy(String text) throws ServiceNotRespondingException {
        try {
            FallacyRequestDto request = new FallacyRequestDto(text);

            FallacyResponseDto response = restTemplate.postForObject("http://fallacy-detector:6161/analyze", request, FallacyResponseDto.class);

            if (response != null) {
                System.out.println("Výsledek z AI: " + response.getLabel());
                System.out.println("Jistota: " + (response.getScore() * 100) + "%");
                return response;
            }
            return null;
        } catch (Exception e) {
            throw new ServiceNotRespondingException("Error with contacting Bert: " + e.getMessage());
        }
    }
}
