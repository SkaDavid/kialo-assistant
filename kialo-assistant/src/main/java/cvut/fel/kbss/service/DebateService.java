package cvut.fel.kbss.service;

import cvut.fel.kbss.client.AIClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.*;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DebateService {
    private final UserRepository userRepository;
    private final DebateRepository debateRepository;
    private final Mapper mapper;
    private final AIClient aiClient;

    @Autowired
    public DebateService(UserRepository userRepository, DebateRepository debateRepository, Mapper mapper, AIClient aiClient){
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.mapper = mapper;
        this.aiClient = aiClient;
    }


    @Transactional
    public DebateResponseDto createDebate(String topic, String thesis, DebateVisibility visibility, String keyCloakId) throws UserNotFoundException {
        Optional<User> ownerOpt = userRepository.findByKeycloakId(keyCloakId);
        if(ownerOpt.isEmpty()){
            throw new UserNotFoundException("User not found");
        }

        User owner = ownerOpt.get();
        Debate debate = new Debate();
        Argument argument = new Argument(thesis, ArgumentType.THESIS, null, owner, debate);
        List<Argument> newList = new ArrayList<>();
        newList.add(argument);

        debate.setOwner(owner);
        debate.setTopic(topic);
        debate.setArguments(newList);
        debate.setVisibility(visibility);

        Debate newDebate = debateRepository.save(debate);
        return mapper.toDto(newDebate);
    }

    @Transactional
    public DebateResponseDto getDebate(Long id, String keyCloakId) throws DebateNotFoundException, UnauthorizedAccessException {
        Optional<Debate> debateOpt = debateRepository.findById(id);
        if(debateOpt.isEmpty()){
            throw new DebateNotFoundException("Debate not found");
        }
        Debate debate = debateOpt.get();

        boolean isPublic = debate.getVisibility().equals(DebateVisibility.PUBLIC);
        boolean isUserDebate = debate.getOwner().getKeycloakId().equals(keyCloakId);
        if(isPublic || isUserDebate){
            return mapper.toDto(debate);
        } else{
            throw new UnauthorizedAccessException("Not authorised to access this debate");
        }
    }

    public List<DebateResponseDto> findAll() {
        List<Debate> debates = debateRepository.findAll();
        return debates.stream()
                .map(debate -> mapper.toDto(debate))
                .collect(Collectors.toList());
    }

    public List<DebateResponseDto> findAllForUser(String keycloakId) {
        List<Debate> debates = debateRepository.findDebatesForUser(keycloakId);
        return debates.stream()
                .map(debate -> mapper.toDto(debate))
                .collect(Collectors.toList());
    }

    @Transactional
    public DebateResponseDto updateDebate(Long id, String newTopic, DebateVisibility visibility, String keycloakId)
            throws DebateNotFoundException, UnauthorizedAccessException {

        Optional<Debate> debateOpt = debateRepository.findById(id);
        if(debateOpt.isEmpty()){
            throw new DebateNotFoundException("Debate not found");
        }
        Debate debate = debateOpt.get();
        if (!debate.getOwner().getKeycloakId().equals(keycloakId)) {
            throw new UnauthorizedAccessException("You are not the owner of this debate");
        }
        debate.setTopic(newTopic);
        debate.setVisibility(visibility);
        debateRepository.save(debate);
        return mapper.toDto(debate);
    }


    public AIDebateResponse generateDebate(String thesis) throws ThesisNotDefinedException, APIkeyNotFoundException, ServiceNotRespondingException {
        if (thesis == null || thesis.isEmpty()) {
            throw new ThesisNotDefinedException("Thesis was not found");
        }
        return this.aiClient.generateDebate(thesis);
    }
}
