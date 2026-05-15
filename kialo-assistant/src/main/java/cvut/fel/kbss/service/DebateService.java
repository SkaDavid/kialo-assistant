package cvut.fel.kbss.service;

import cvut.fel.kbss.client.DebateGenerationClient;
import cvut.fel.kbss.client.TermitClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.dto.response.DebateInfoDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.*;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DebateService {
    private final UserRepository userRepository;
    private final DebateRepository debateRepository;
    private final Mapper mapper;
    private final DebateGenerationClient debateGenerationClient;
    private final TermitClient termitClient;
    private final ArgumentService argumentService;

    @Autowired
    public DebateService(UserRepository userRepository, DebateRepository debateRepository, Mapper mapper, DebateGenerationClient debateGenerationClient, TermitClient termitClient, ArgumentService argumentService){
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.mapper = mapper;
        this.debateGenerationClient = debateGenerationClient;
        this.termitClient = termitClient;
        this.argumentService = argumentService;
    }


    @Transactional
    public DebateResponseDto createDebate(String topic, String thesis, DebateVisibility visibility, JwtAuthenticationToken token) throws UserNotFoundException, ServiceNotRespondingException {
        User owner = userRepository.findByKeycloakId(token.getToken().getSubject()).orElseThrow(() -> new UserNotFoundException("User not found"));

        Debate debate = new Debate();
        Argument argument = new Argument(thesis, ArgumentType.THESIS, null, owner, debate);
        TextSegment segment = new TextSegment(TextSegmentType.TEXT, thesis, null, null);
        List<TextSegment> segments = new ArrayList<>();
        segments.add(segment);
        argument.setSegments(segments);
        List<Argument> newList = new ArrayList<>();
        newList.add(argument);

        debate.setOwner(owner);
        debate.setTopic(topic);
        debate.setArguments(newList);
        debate.setVisibility(visibility);

        Debate newDebate = debateRepository.save(debate);
        termitClient.createDictionary(topic, newDebate.getId(), token.getToken().getTokenValue());
        termitClient.createArgumentFile(thesis, debate.getId(), newDebate.getArguments().getFirst().getId(), token.getToken().getTokenValue());
        return mapper.toDto(newDebate);
    }

    @Transactional
    public DebateResponseDto getDebate(Long id, String keyCloakId) throws DebateNotFoundException, UnauthorizedAccessException {
        Debate debate = debateRepository.findById(id).orElseThrow(() -> new DebateNotFoundException("Debate not found"));

        boolean isPublic = debate.getVisibility().equals(DebateVisibility.PUBLIC);
        boolean isUserDebate = debate.getOwner().getKeycloakId().equals(keyCloakId);
        if(isPublic || isUserDebate){
            return mapper.toDto(debate);
        } else{
            throw new UnauthorizedAccessException("Not authorised to access this debate");
        }
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

        Debate debate = debateRepository.findById(id).orElseThrow(() -> new DebateNotFoundException("Debate not found"));
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
        return this.debateGenerationClient.generateDebate(thesis);
    }

    @Transactional
    public DebateResponseDto saveGeneratedDebate(AIDebateResponse dto, JwtAuthenticationToken token) throws UserNotFoundException, ServiceNotRespondingException {
        User owner = userRepository.findByKeycloakId(token.getToken().getSubject()).orElseThrow(() -> new UserNotFoundException("User not found"));

        Debate debate = new Debate();
        debate.setTopic(dto.getTopic());
        debate.setOwner(owner);
        debate.setVisibility(DebateVisibility.PRIVATE);
        debate.setKialoId(dto.getDebateId());
        debate.setArguments(new ArrayList<>());

        final Debate savedDebate = debateRepository.save(debate);

        termitClient.createDictionary(dto.getTopic(), savedDebate.getId(), token.getToken().getTokenValue());

        argumentService.saveArgumentTree(dto.getArguments(), savedDebate, owner, token);

        return mapper.toDto(savedDebate);
    }

    public DebateInfoDto getDebateInfo(Long kialoDebateId) {
        Debate debate = debateRepository.findDebateByKialoId(kialoDebateId);
        if(debate != null){
            return mapper.toDebateInfoDto(debate);
        }
        return new DebateInfoDto(false, null, null);
    }
}
