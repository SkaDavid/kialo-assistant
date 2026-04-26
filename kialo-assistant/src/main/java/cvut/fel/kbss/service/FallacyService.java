package cvut.fel.kbss.service;

import cvut.fel.kbss.dto.request.FallacyRequestDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FallacyService {
    private final RestTemplate restTemplate;

    @Autowired
    public FallacyService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public FallacyResponseDto testFallacy(String text){
        try {
            System.out.println("SENT TEXT " + text);
            FallacyRequestDto request = new FallacyRequestDto(text);

            FallacyResponseDto response = restTemplate.postForObject("http://fallacy-detector:6161/analyze", request, FallacyResponseDto.class);

            if (response != null) {
                System.out.println("Výsledek z AI: " + response.getLabel());
                System.out.println("Jistota: " + (response.getScore() * 100) + "%");
                return response;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Chyba při komunikaci s Python službou: " + e.getMessage());
        }
        return null;
    }
}
