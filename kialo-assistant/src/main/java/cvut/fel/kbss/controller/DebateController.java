package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.request.NewDebateDto;
import cvut.fel.kbss.dto.request.UpdateDebateDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.service.DebateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
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
            throws UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating debate with {} topic", keycloakId, dto.getTopic());

        DebateResponseDto response = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                dto.getVisibility(),
                keycloakId
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

    // TODO zabezpecit
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




    /*  old ai stuff  */

    @PostMapping(value = "/ai")
    public ResponseEntity<String> postAI(@RequestBody String thesis)
            throws APIkeyNotFoundException, OpenAINotRespondingException, ThesisNotDefinedException {
        String result;
        result = debateService.generateDebate(thesis);
        return ResponseEntity.ok(result);
    }
}
