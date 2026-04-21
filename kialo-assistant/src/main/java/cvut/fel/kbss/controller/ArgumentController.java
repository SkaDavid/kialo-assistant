package cvut.fel.kbss.controller;


import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.request.NewArgumentDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.service.ArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ArgumentResponseDto> createArgument(@RequestBody NewArgumentDto dto){
        ArgumentResponseDto response = this.argumentService.createArgument(dto.getText(), dto.getType(), dto.getParentId(), dto.getDebateId(), dto.getUserId());
        if(response != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.badRequest().build();
    }
}
