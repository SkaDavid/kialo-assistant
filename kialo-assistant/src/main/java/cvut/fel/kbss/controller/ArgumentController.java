package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.request.NewArgumentDto;
import cvut.fel.kbss.dto.request.UpdateArgumentDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.exception.ArgumentNotFoundException;
import cvut.fel.kbss.exception.DebateNotFoundException;
import cvut.fel.kbss.exception.UnauthorizedAccessException;
import cvut.fel.kbss.exception.UserNotFoundException;
import cvut.fel.kbss.service.ArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    public ResponseEntity<ArgumentResponseDto> createArgument(@RequestBody NewArgumentDto dto)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException {
        ArgumentResponseDto response = this.argumentService.createArgument(dto.getText(), dto.getType(), dto.getParentId(), dto.getDebateId(), dto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArgumentResponseDto> updateArgument(@PathVariable Long id, @RequestBody UpdateArgumentDto dto, JwtAuthenticationToken token)
            throws ArgumentNotFoundException, UnauthorizedAccessException {

        String keycloakId = token.getToken().getSubject();
        ArgumentResponseDto argument = argumentService.updateArgument(id, dto.getText(), keycloakId);
        return ResponseEntity.ok(argument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArgument(@PathVariable Long id, JwtAuthenticationToken token)
            throws UnauthorizedAccessException, ArgumentNotFoundException, UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        this.argumentService.deleteArgument(id, keycloakId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
