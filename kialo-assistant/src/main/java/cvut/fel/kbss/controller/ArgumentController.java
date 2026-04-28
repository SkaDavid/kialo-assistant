package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.request.FallacyRequestDto;
import cvut.fel.kbss.dto.request.NewArgumentDto;
import cvut.fel.kbss.dto.request.UpdateArgumentDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.exception.ArgumentNotFoundException;
import cvut.fel.kbss.exception.DebateNotFoundException;
import cvut.fel.kbss.exception.UnauthorizedAccessException;
import cvut.fel.kbss.exception.UserNotFoundException;
import cvut.fel.kbss.service.ArgumentService;
import cvut.fel.kbss.service.FallacyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("argument")
public class ArgumentController {
    private final ArgumentService argumentService;
    private final FallacyService fallacyService;
    @Autowired
    public ArgumentController(ArgumentService argumentService, FallacyService fallacyService){
        this.argumentService = argumentService;
        this.fallacyService = fallacyService;
    }

    @PostMapping
    public ResponseEntity<ArgumentResponseDto> createArgument(@RequestBody NewArgumentDto dto, JwtAuthenticationToken token)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating new argument", keycloakId);

        ArgumentResponseDto response = this.argumentService.createArgument(dto.getText(), dto.getType(), dto.getParentId(), dto.getDebateId(), keycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArgumentResponseDto> updateArgument(@PathVariable Long id, @RequestBody UpdateArgumentDto dto, JwtAuthenticationToken token)
            throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is updating argument {}", keycloakId, id);

        ArgumentResponseDto argument = argumentService.updateArgument(id, dto.getText(), dto.getType(), keycloakId);
        return ResponseEntity.ok(argument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArgument(@PathVariable Long id, JwtAuthenticationToken token)
            throws UnauthorizedAccessException, ArgumentNotFoundException, UserNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is deleting argument {}", keycloakId, id);

        this.argumentService.deleteArgument(id, keycloakId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/fallacy")
    public ResponseEntity<FallacyResponseDto> createArgument(@RequestBody FallacyRequestDto dto) {
        FallacyResponseDto fallacyResponse = fallacyService.testFallacy(dto.getText());

        return ResponseEntity.status(HttpStatus.OK).body(fallacyResponse);
    }
}
