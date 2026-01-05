package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.TermDTO;
import cvut.fel.kbss.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("test")
public class TestController {
    private final TestService ts;

    @Autowired
    public TestController(TestService ts){
        this.ts = ts;
    }

    @GetMapping
    public ResponseEntity<String> getTest(){
        String response = ts.getTest();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/ai")
    public ResponseEntity<String> postAI(@RequestBody String thesis){
        String resultOk = ts.createDebate(thesis);
        if(!resultOk.isEmpty()){
            return ResponseEntity.ok(resultOk);
        } else return ResponseEntity.badRequest().build();
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(value = "/termit")
    public ResponseEntity<String> getTermitData(@RequestBody TermDTO termDto){
        String data;
        try{
            data = ts.getTermitData(termDto.getLabel(), termDto.getDescription(), termDto.getAuthorization());
        } catch(Exception e){
            data = e.getMessage();
            return ResponseEntity.badRequest().body(data);
        }
        return ResponseEntity.ok(data);
    }
}
