package cvut.fel.kbss.service;


import cvut.fel.kbss.client.AIClient;
import cvut.fel.kbss.client.FallacyClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.AIArgumentResponseDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
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
    private final AIClient aiClient;

    @Autowired
    public ArgumentService(ArgumentRepository argumentRepository, UserRepository userRepository, DebateRepository debateRepository, FallacyClient fallacyClient, Mapper mapper, AIClient aiClient){
        this.argumentRepository = argumentRepository;
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.fallacyClient = fallacyClient;
        this.mapper = mapper;
        this.aiClient = aiClient;
    }

    @Transactional
    public ArgumentResponseDto createArgument(String text, ArgumentType type, Long parentId, Long debateId, String userId)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException {
        Optional<User> ownerOpt = userRepository.findByKeycloakId(userId);
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

    public FallacyResponseDto testFallacy(String text) throws ServiceNotRespondingException {
        return fallacyClient.testFallacy(text);
    }

    public AIArgumentResponseDto generateArgument(String text, String type, List<ArgumentResponseDto> debate) throws APIkeyNotFoundException, ServiceNotRespondingException {
        String debateString = formatDebateToJson(debate);
        String newArgumentText = aiClient.generateArgument(text, type, debateString);

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
