package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.request.NewDebateDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.service.DebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<DebateResponseDto> createDebate(@RequestBody NewDebateDto dto, JwtAuthenticationToken token) throws UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        DebateResponseDto response = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                keycloakId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DebateResponseDto>> getDebates(){
        List<DebateResponseDto> debates = debateService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(debates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebateResponseDto> getDebate(@PathVariable Long id) throws DebateNotFoundException {
        DebateResponseDto debate = debateService.getDebate(id);
        return ResponseEntity.status(HttpStatus.OK).body(debate);
    }








    /*  old ai stuff  */

    @PostMapping(value = "/ai")
    public ResponseEntity<String> postAI(@RequestBody String thesis) throws APIkeyNotFoundException, OpenAINotRespondingException, ThesisNotDefinedException {
        String result;
        result = debateService.generateDebate(thesis);
        return ResponseEntity.ok(result);
    }
}
