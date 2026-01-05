package cvut.fel.kbss.controller;

import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.OpenAINotRespondingException;
import cvut.fel.kbss.exception.ThesisNotDefinedException;
import cvut.fel.kbss.service.DebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("debate")
public class DebateController {
    private final DebateService ds;

    @Autowired
    public DebateController(DebateService ds){
        this.ds = ds;
    }
    @PostMapping(value = "/ai")
    public ResponseEntity<String> postAI(@RequestBody String thesis){
        String result;
        try{
            result = ds.createDebate(thesis);
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
