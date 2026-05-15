package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.request.NewDebateDto;
import cvut.fel.kbss.dto.request.UpdateDebateDto;
import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.service.DebateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("debate")
public class DebateController {
    private final DebateService debateService;
    @Autowired
    public DebateController(DebateService debateService){
        this.debateService = debateService;
    }

    @PostMapping
    public ResponseEntity<DebateResponseDto> createDebate(@RequestBody NewDebateDto dto, JwtAuthenticationToken token)
            throws UserNotFoundException, ServiceNotRespondingException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating debate with {} topic", keycloakId, dto.getTopic());

        DebateResponseDto response = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                dto.getVisibility(),
                token
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DebateResponseDto>> getDebates(JwtAuthenticationToken token){
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is requesting list of debates", keycloakId);

        List<DebateResponseDto> debates = debateService.findAllForUser(keycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(debates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebateResponseDto> getDebate(@PathVariable Long id, JwtAuthenticationToken token)
            throws DebateNotFoundException, UnauthorizedAccessException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is requesting debate {}", keycloakId, id);

        DebateResponseDto debate = debateService.getDebate(id, keycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(debate);
    }


    @PutMapping("/{id}")
    public ResponseEntity<DebateResponseDto> updateDebate(@PathVariable Long id, @RequestBody UpdateDebateDto dto, JwtAuthenticationToken token)
            throws DebateNotFoundException, UnauthorizedAccessException  {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is updating debate {}", keycloakId, id);

        DebateResponseDto debate = debateService.updateDebate(id, dto.getTopic(), dto.getVisibility(), keycloakId);
        return ResponseEntity.ok(debate);
    }

    @PostMapping(value = "/ai")
    public ResponseEntity<AIDebateResponse> postAI(@RequestBody NewDebateDto dto, JwtAuthenticationToken token)
            throws APIkeyNotFoundException, ServiceNotRespondingException, ThesisNotDefinedException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating debate with help of AI", keycloakId);

        AIDebateResponse result;
        result = debateService.generateDebate(dto.getThesis());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import-debate")
    public ResponseEntity<DebateResponseDto> importDebate(@RequestBody AIDebateResponse dto, JwtAuthenticationToken token)
            throws UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is importing AI generated debate: {}", keycloakId, dto.getTopic());

        DebateResponseDto response = debateService.saveGeneratedDebate(dto, keycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
