package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.NewArgumentDto;
import cvut.fel.kbss.dto.NewDebateDto;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.OpenAINotRespondingException;
import cvut.fel.kbss.exception.ThesisNotDefinedException;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.service.DebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> createDebate(@RequestBody NewDebateDto dto){
        String result = this.debateService.createDebate(
                dto.getTopic(),
                dto.getThesis(),
                dto.getOwnerId()
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

    @GetMapping("/{topic}")
    public ResponseEntity<Debate> getDebateByTopic(@PathVariable String topic){
        Debate debate = debateService.getDebateByTopic(topic);
        return ResponseEntity.ok(debate);
    }

    @PostMapping("/argument")
    public ResponseEntity<String> createArgument(@RequestBody NewArgumentDto dto){
        String response = this.debateService.createArgument(dto.getText(), dto.getType(), dto.getParentId(), dto.getDebateId(), dto.getUserId());
        if(response != null){
            return ResponseEntity.ok("Ayo");
        }
        return ResponseEntity.badRequest().build();
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
