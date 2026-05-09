package cvut.fel.kbss.service;


import cvut.fel.kbss.client.DebateGenerationClient;
import cvut.fel.kbss.client.ExplanationClient;
import cvut.fel.kbss.client.FallacyClient;
import cvut.fel.kbss.client.TermitClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.AIArgumentResponseDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.dto.response.ValidationResponse;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.ArgumentType;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArgumentService {
    private final ArgumentRepository argumentRepository;
    private final UserRepository userRepository;
    private final DebateRepository debateRepository;
    private final FallacyClient fallacyClient;
    private final Mapper mapper;
    private final DebateGenerationClient debateGenerationClient;
    private final ExplanationClient explanationClient;
    private final TermitClient termitClient;

    @Autowired
    public ArgumentService(ArgumentRepository argumentRepository, UserRepository userRepository, DebateRepository debateRepository, FallacyClient fallacyClient, Mapper mapper, DebateGenerationClient debateGenerationClient, ExplanationClient explanationClient, TermitClient termitClient){
        this.argumentRepository = argumentRepository;
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.fallacyClient = fallacyClient;
        this.mapper = mapper;
        this.debateGenerationClient = debateGenerationClient;
        this.explanationClient = explanationClient;
        this.termitClient = termitClient;
    }

    @Transactional
    public ArgumentResponseDto createArgument(String text, ArgumentType type, Long parentId, Long debateId, JwtAuthenticationToken token)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException, ServiceNotRespondingException {
        Optional<User> ownerOpt = userRepository.findByKeycloakId(token.getToken().getSubject());
        Optional<Argument> parentOpt = argumentRepository.findById(parentId);
        Optional<Debate> debateOpt = debateRepository.findById(debateId);
        if(ownerOpt.isEmpty()){
            throw new UserNotFoundException("Owner not found");
        }
        if(parentOpt.isEmpty()){
            throw new ArgumentNotFoundException("Parent not found");
        }
        if(debateOpt.isEmpty()){
            throw new DebateNotFoundException("Debate not found");
        }
        Debate debate = debateOpt.get();
        User owner = ownerOpt.get();
        Argument parent = parentOpt.get();

        Argument argument = new Argument();
        argument.setDebate(debate);
        argument.setOwner(owner);
        argument.setParent(parent);
        argument.setType(type);
        argument.setText(text);

        List<Argument> debateArguments = debate.getArguments();
        debateArguments.add(argument);
        debate.setArguments(debateArguments);
        Argument newArgument = argumentRepository.save(argument);

        termitClient.createArgumentFile(text, debateId, newArgument.getId(), token.getToken().getTokenValue());

        return mapper.toDto(newArgument);
    }

    @Transactional
    public void deleteArgument(Long argumentId, String keycloakId) throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException {
        Optional<Argument> argumentOpt = argumentRepository.findById(argumentId);
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if(argumentOpt.isEmpty()){
            throw new ArgumentNotFoundException("Argument you wish to delete was not found");
        }
        if(userOpt.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        Argument argument = argumentOpt.get();
        User user = userOpt.get();

        if(!argument.getOwner().getKeycloakId().equals(user.getKeycloakId())){
            throw new UnauthorizedAccessException("User is not the owner of the argument");
        }
        argumentRepository.deleteById(argumentId);
    }

    @Transactional
    public ArgumentResponseDto updateArgument(Long id, String newText, ArgumentType newType, String keycloakId)
            throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException {

        Optional<Argument> argumentOpt = argumentRepository.findById(id);
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if(argumentOpt.isEmpty()){
            throw new ArgumentNotFoundException("Argument not found");
        }
        if(userOpt.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        User user = userOpt.get();
        Argument argument = argumentOpt.get();
        if (!argument.getOwner().getKeycloakId().equals(user.getKeycloakId())) {
            throw new UnauthorizedAccessException("You are not the owner of this argument");
        }
        argument.setText(newText);
        argument.setType(newType);
        argumentRepository.save(argument);
        return mapper.toDto(argument);
    }

    public FallacyResponseDto testFallacy(String text) throws ServiceNotRespondingException, APIkeyNotFoundException {
        FallacyResponseDto fallacyTest = fallacyClient.testFallacy(text);
        if(fallacyTest.getScore() > 0.75){
            ValidationResponse validation = explanationClient.explainFallacy(fallacyTest.getLabel(), text);
            fallacyTest.setFallacy(validation.isFallacy());
            fallacyTest.setExplanation(validation.getExplanation());
        } else {
            fallacyTest.setExplanation("No fallacy detected");
            fallacyTest.setFallacy(false);
        }
        return fallacyTest;
    }

    public AIArgumentResponseDto generateArgument(String text, String type, List<ArgumentResponseDto> debate) throws APIkeyNotFoundException, ServiceNotRespondingException {
        String debateString = formatDebateToJson(debate);
        String newArgumentText = debateGenerationClient.generateArgument(text, type, debateString);

        AIArgumentResponseDto result = new AIArgumentResponseDto();
        result.setText(newArgumentText);
        return result;
    }

    public String formatDebateToJson(List<ArgumentResponseDto> contextArguments) {
        JSONArray jsonArray = new JSONArray();

        for (ArgumentResponseDto arg : contextArguments) {
            JSONObject obj = new JSONObject();
            obj.put("id", arg.getId());
            obj.put("parent", arg.getParent());
            obj.put("type", arg.getType());
            obj.put("text", arg.getText());

            jsonArray.put(obj);
        }
        return jsonArray.toString(4);
    }
}
