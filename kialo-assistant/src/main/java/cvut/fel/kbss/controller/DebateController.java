package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.NewArgumentDto;
import cvut.fel.kbss.dto.NewDebateDto;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.OpenAINotRespondingException;
import cvut.fel.kbss.exception.ThesisNotDefinedException;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.service.DebateService;
import cvut.fel.kbss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("debate")
public class DebateController {
    private final DebateService debateService;
    private final UserService userService;
    @Autowired
    public DebateController(DebateService debateService, UserService userService){
        this.debateService = debateService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createDebate(@RequestBody NewDebateDto dto, JwtAuthenticationToken token){
        String keycloakId = token.getToken().getSubject();
        String result = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                keycloakId
        );
        if(result != null){
            return ResponseEntity.ok("Great");
        }
        else return ResponseEntity.badRequest().build();
    }

    @GetMapping
    public ResponseEntity<List<Debate>> getDebates(){
        List<Debate> debates = debateService.findAll();
        if(!debates.isEmpty()){
            return ResponseEntity.ok(debates);
        } else return ResponseEntity.badRequest().build();

    }

    @GetMapping("/{id}")
    public ResponseEntity<Debate> getDebate(@PathVariable Long id){
        Debate debate = debateService.getDebate(id);
        return ResponseEntity.ok(debate);
    }


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
