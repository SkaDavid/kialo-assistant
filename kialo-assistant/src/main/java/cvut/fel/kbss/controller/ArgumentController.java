package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.request.AIArgumentRequestDto;
import cvut.fel.kbss.dto.request.FallacyRequestDto;
import cvut.fel.kbss.dto.request.NewArgumentDto;
import cvut.fel.kbss.dto.request.UpdateArgumentDto;
import cvut.fel.kbss.dto.response.AIArgumentResponseDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.service.ArgumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    public ResponseEntity<ArgumentResponseDto> createArgument(@RequestBody NewArgumentDto dto, JwtAuthenticationToken token)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException, ServiceNotRespondingException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating new argument", keycloakId);

        ArgumentResponseDto response = this.argumentService.createArgument(dto.getText(), dto.getType(), dto.getParentId(), dto.getDebateId(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArgumentResponseDto> getArgument(@PathVariable Long id, JwtAuthenticationToken token) throws UserNotFoundException, UnauthorizedAccessException, ArgumentNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is requesting argument {}", keycloakId, id);

        ArgumentResponseDto argument = argumentService.getArgument(id, token.getToken().getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(argument);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArgumentResponseDto> updateArgument(@PathVariable Long id, @RequestBody UpdateArgumentDto dto, JwtAuthenticationToken token)
            throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException, ServiceNotRespondingException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is updating argument {}", keycloakId, id);

        ArgumentResponseDto argument = argumentService.updateArgument(id, dto.getText(), dto.getType(), token);
        return ResponseEntity.ok(argument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArgument(@PathVariable Long id, JwtAuthenticationToken token)
            throws UnauthorizedAccessException, ArgumentNotFoundException, UserNotFoundException, ServiceNotRespondingException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is deleting argument {}", keycloakId, id);

        this.argumentService.deleteArgument(id, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/fallacy")
    public ResponseEntity<FallacyResponseDto> testFallacy(@RequestBody FallacyRequestDto dto, JwtAuthenticationToken token) throws ServiceNotRespondingException, APIkeyNotFoundException, ArgumentNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is checking for fallacy with this text: {}", keycloakId, dto.getText());

        FallacyResponseDto fallacyResponse = argumentService.testFallacy(dto.getText(), dto.getArgumentId());
        return ResponseEntity.status(HttpStatus.OK).body(fallacyResponse);
    }

    @PostMapping("/ai")
    public ResponseEntity<AIArgumentResponseDto> createArgumentUsingAI(@RequestBody AIArgumentRequestDto dto, JwtAuthenticationToken token) throws APIkeyNotFoundException, ServiceNotRespondingException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is creating new argument using AI", keycloakId);

        AIArgumentResponseDto result = argumentService.generateArgument(dto.getText(), dto.getType(), dto.getDebate());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/sync-termit/{argumentId}")
    public ResponseEntity<ArgumentResponseDto> synchronizeTermitArgument(@PathVariable long argumentId, JwtAuthenticationToken token) throws ServiceNotRespondingException, ArgumentNotFoundException {
        String keycloakId = token.getToken().getSubject();
        log.info("User {} is requesting termit sync on argument: " + argumentId, keycloakId);

        ArgumentResponseDto response = argumentService.syncWithTermit(argumentId, token.getToken().getTokenValue());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
