package cvut.fel.kbss.controller;


import cvut.fel.kbss.dto.NewArgumentDto;
import cvut.fel.kbss.service.ArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("argument")
public class ArgumentController {
    private final ArgumentService argumentService;
    @Autowired
    public ArgumentController(ArgumentService argumentService){
        this.argumentService = argumentService;
    }

    @PostMapping
    public ResponseEntity<String> createArgument(@RequestBody NewArgumentDto dto){
        return ResponseEntity.ok("Good");
    }
}
