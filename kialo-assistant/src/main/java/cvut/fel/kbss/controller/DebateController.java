package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.request.NewDebateDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.OpenAINotRespondingException;
import cvut.fel.kbss.exception.ThesisNotDefinedException;
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
    public ResponseEntity<DebateResponseDto> createDebate(@RequestBody NewDebateDto dto, JwtAuthenticationToken token){
        String keycloakId = token.getToken().getSubject();
        DebateResponseDto response = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                keycloakId
        );
        if(response != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        //TODO Exe
        else return ResponseEntity.badRequest().build();
    }

    @GetMapping
    public ResponseEntity<List<DebateResponseDto>> getDebates(){
        List<DebateResponseDto> debates = debateService.findAll();
        if(!debates.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(debates);
        } else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    }

    @GetMapping("/{id}")
    public ResponseEntity<DebateResponseDto> getDebate(@PathVariable Long id){
        DebateResponseDto debate = debateService.getDebate(id);
        if(debate != null){
            return ResponseEntity.status(HttpStatus.OK).body(debate);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }








    /* TODO old ai stuff  */

    @PostMapping(value = "/ai")
    public ResponseEntity<String> postAI(@RequestBody String thesis){
        String result;
        try{
            result = debateService.generateDebate(thesis);
        } catch(ThesisNotDefinedException e){
            return ResponseEntity.badRequest().build();
        } catch(OpenAINotRespondingException | APIkeyNotFoundException e){
            return ResponseEntity.internalServerError().build();
        }
        if(!result.isEmpty()){
            return ResponseEntity.ok(result);
        } else return ResponseEntity.badRequest().build();
    }
}
