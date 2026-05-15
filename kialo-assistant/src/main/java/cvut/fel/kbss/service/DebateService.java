package cvut.fel.kbss.service;

import cvut.fel.kbss.client.DebateGenerationClient;
import cvut.fel.kbss.client.TermitClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateInfoDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.*;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
    private final ArgumentRepository argumentRepository;
    private final Mapper mapper;
    private final DebateGenerationClient debateGenerationClient;
    private final TermitClient termitClient;

    @Autowired
    public DebateService(UserRepository userRepository, DebateRepository debateRepository, ArgumentRepository argumentRepository, Mapper mapper, DebateGenerationClient debateGenerationClient, TermitClient termitClient){
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.argumentRepository = argumentRepository;
        this.mapper = mapper;
        this.debateGenerationClient = debateGenerationClient;
        this.termitClient = termitClient;
    }


    @Transactional
    public DebateResponseDto createDebate(String topic, String thesis, DebateVisibility visibility, JwtAuthenticationToken token) throws UserNotFoundException, ServiceNotRespondingException {
        Optional<User> ownerOpt = userRepository.findByKeycloakId(token.getToken().getSubject());
        if(ownerOpt.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        //TODO
        User owner = ownerOpt.get();
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
        return this.debateGenerationClient.generateDebate(thesis);
    }

    @Transactional
    public DebateResponseDto saveGeneratedDebate(AIDebateResponse dto, String keycloakId) throws UserNotFoundException {
        Optional<User> ownerOpt = userRepository.findByKeycloakId(keycloakId);
        if (ownerOpt.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User owner = ownerOpt.get();

        Debate debate = new Debate();
        debate.setTopic(dto.getTopic());
        debate.setOwner(owner);
        debate.setVisibility(DebateVisibility.PRIVATE);
        debate.setArguments(new ArrayList<>());
        debate.setKialoId(dto.getDebateId());
        Debate savedDebate = debateRepository.save(debate);

        Map<Long, Argument> idMapping = new HashMap<>();
        List<ArgumentResponseDto> remainingArguments = new ArrayList<>(dto.getArguments());

        ArgumentResponseDto thesisDto = remainingArguments.stream()
                .filter(argument -> "THESIS".equals(argument.getType()))
                .findFirst()
                .get();

        Argument thesis = new Argument();
        thesis.setText(thesisDto.getText());
        thesis.setType(ArgumentType.THESIS);
        thesis.setParent(null);
        thesis.setOwner(owner);
        thesis.setDebate(savedDebate);
        thesis.setSegments(this.parseHtmlToSegments(thesisDto.getText()));
        thesis.setKialoId(thesisDto.getId());
        thesis.setKialoVersion(thesisDto.getVersion());

        Argument savedThesis = argumentRepository.save(thesis);
        idMapping.put(thesisDto.getId(), savedThesis);
        remainingArguments.remove(thesisDto);

        while (!remainingArguments.isEmpty()) {
            List<ArgumentResponseDto> toRemove = new ArrayList<>();
            for (ArgumentResponseDto argumentDto : remainingArguments) {
                if (idMapping.containsKey(argumentDto.getParent())) {
                    Argument parentEntity = idMapping.get(argumentDto.getParent());

                    Argument newArgument = new Argument();
                    newArgument.setText(argumentDto.getText());
                    newArgument.setType(ArgumentType.valueOf(argumentDto.getType()));
                    newArgument.setParent(parentEntity);
                    newArgument.setOwner(owner);
                    newArgument.setDebate(savedDebate);
                    newArgument.setSegments(parseHtmlToSegments(argumentDto.getText()));
                    newArgument.setKialoId(argumentDto.getId());
                    newArgument.setKialoVersion(argumentDto.getVersion());

                    Argument savedArg = argumentRepository.save(newArgument);
                    idMapping.put(argumentDto.getId(), savedArg);
                    toRemove.add(argumentDto);
                }
            }
            remainingArguments.removeAll(toRemove);
        }
        return mapper.toDto(savedDebate);
    }

    private List<TextSegment> parseHtmlToSegments(String htmlContent) {
        List<TextSegment> segments = new ArrayList<>();
        Document document = Jsoup.parse(htmlContent);
        Element body = document.body();
        for(Node node : body.childNodes()){
            if (node instanceof TextNode) {
                String content = ((TextNode) node).getWholeText();
                if (!content.isBlank()) {
                    segments.add(new TextSegment(TextSegmentType.TEXT, content, null, null));
                }
            } else if (node instanceof Element element) {
                if (element.tagName().equals("span") && element.hasAttr("resource")) {
                    segments.add(new TextSegment(
                            TextSegmentType.TERM,
                            element.text(),
                            null,
                            element.attr("resource")
                    ));
                } else {
                    segments.add(new TextSegment(TextSegmentType.TEXT, element.text(), null, null));
                }
            }
        }
        return segments;
    }

    public DebateInfoDto getDebateInfo(Long kialoDebateId) {
        Debate debate = debateRepository.findDebateByKialoId(kialoDebateId);
        if(debate != null){
            return mapper.toDebateInfoDto(debate);
        }
        return new DebateInfoDto(false, null, null);
    }
}
