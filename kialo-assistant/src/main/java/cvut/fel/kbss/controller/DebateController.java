package cvut.fel.kbss.controller;

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
        String resultOk = ds.createDebate(thesis);
        if(!resultOk.isEmpty()){
            return ResponseEntity.ok(resultOk);
        } else return ResponseEntity.badRequest().build();
    }
}
