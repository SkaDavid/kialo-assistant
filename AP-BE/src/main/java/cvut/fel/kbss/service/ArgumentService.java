package cvut.fel.kbss.service;


import cvut.fel.kbss.client.DebateGenerationClient;
import cvut.fel.kbss.client.ExplanationClient;
import cvut.fel.kbss.client.FallacyClient;
import cvut.fel.kbss.client.TermitClient;
import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.request.NewArgumentDto;
import cvut.fel.kbss.dto.response.AIArgumentResponseDto;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.dto.response.ValidationResponse;
import cvut.fel.kbss.exception.*;
import cvut.fel.kbss.model.*;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import cvut.fel.kbss.util.HtmlParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final HtmlParser htmlParser;

    @Autowired
    public ArgumentService(ArgumentRepository argumentRepository, UserRepository userRepository, DebateRepository debateRepository, FallacyClient fallacyClient, Mapper mapper, DebateGenerationClient debateGenerationClient, ExplanationClient explanationClient, TermitClient termitClient, HtmlParser htmlParser){
        this.argumentRepository = argumentRepository;
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.fallacyClient = fallacyClient;
        this.mapper = mapper;
        this.debateGenerationClient = debateGenerationClient;
        this.explanationClient = explanationClient;
        this.termitClient = termitClient;
        this.htmlParser = htmlParser;
    }

    @Transactional
    public ArgumentResponseDto createArgument(NewArgumentDto dto, JwtAuthenticationToken token)
            throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException, ServiceNotRespondingException {
        User owner = userRepository.findByKeycloakId(token.getToken().getSubject()).orElseThrow(() -> new UserNotFoundException("Owner not found"));
        Argument parent = argumentRepository.findById(dto.getParentId()).orElseThrow(() -> new ArgumentNotFoundException("Parent not found"));
        Debate debate = debateRepository.findById(dto.getDebateId()).orElseThrow(() -> new DebateNotFoundException("Debate not found"));

        Argument argument = new Argument();
        argument.setDebate(debate);
        argument.setOwner(owner);
        argument.setParent(parent);
        argument.setType(dto.getType());
        argument.setText(dto.getText());
        argument.setFallacyCheck(new FallacyCheck());
        argument.setSegments(htmlParser.parseHtmlToSegments(dto.getText()));
        if(dto.getKialoId() != null){
            argument.setKialoId(dto.getKialoId());
            argument.setKialoVersion(dto.getVersion());
        }

        List<Argument> debateArguments = debate.getArguments();
        debateArguments.add(argument);
        debate.setArguments(debateArguments);
        Argument newArgument = argumentRepository.save(argument);

        termitClient.createArgumentFile(newArgument.getText(), newArgument.getDebate().getId(), newArgument.getId(), token.getToken().getTokenValue());

        return mapper.toDto(newArgument);
    }

    @Transactional
    public void deleteArgument(Long argumentId, JwtAuthenticationToken token) throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException, ServiceNotRespondingException {
        Argument argument = argumentRepository.findById(argumentId).orElseThrow(() -> new ArgumentNotFoundException("Argument you wish to delete was not found"));
        User user = userRepository.findByKeycloakId(token.getToken().getSubject()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(!argument.getOwner().getKeycloakId().equals(user.getKeycloakId())){
            throw new UnauthorizedAccessException("User is not the owner of the argument");
        }
        if(argument.getType().equals(ArgumentType.THESIS)){
            throw new UnauthorizedAccessException("It is not possible to delete Thesis");
        }

        long debateId = argument.getDebate().getId();
        termitClient.deleteArgumentFile(debateId, argumentId, token.getToken().getTokenValue());
        argumentRepository.deleteById(argumentId);
    }

    @Transactional
    public ArgumentResponseDto updateArgument(long argumentId, String newText, ArgumentType newType, Integer kialoVersion, JwtAuthenticationToken token)
            throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException, ServiceNotRespondingException {

        Argument argument = argumentRepository.findById(argumentId).orElseThrow(() -> new ArgumentNotFoundException("Argument not found"));
        User user = userRepository.findByKeycloakId(token.getToken().getSubject()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!argument.getOwner().getKeycloakId().equals(user.getKeycloakId())) {
            throw new UnauthorizedAccessException("You are not the owner of this argument");
        }
        if(argument.getText().equals(newText) && argument.getType().equals(newType)){
            return mapper.toDto(argument);
        }
        argument.setText(newText);
        argument.setType(newType);
        argument.setSegments(htmlParser.parseHtmlToSegments(newText));
        argument.setFallacyCheck(new FallacyCheck());
        argument.setKialoVersion(kialoVersion);

        long debateId = argument.getDebate().getId();
        termitClient.updateArgumentFile(debateId, argumentId, newText, token.getToken().getTokenValue());
        argumentRepository.save(argument);
        return mapper.toDto(argument);
    }

    @Transactional
    public FallacyResponseDto testFallacy(String text, Long argumentId) throws ServiceNotRespondingException, APIkeyNotFoundException, ArgumentNotFoundException {
        FallacyResponseDto fallacyTest = fallacyClient.testFallacy(text);

        if (fallacyTest.getScore() > 0.75) {
            ValidationResponse validation = explanationClient.explainFallacy(fallacyTest.getLabel(), text);
            fallacyTest.setFallacy(validation.isFallacy());
            fallacyTest.setExplanation(validation.getExplanation());
        } else {
            fallacyTest.setExplanation("No fallacy detected");
            fallacyTest.setFallacy(false);
        }

        if (argumentId != null) {
            Argument argument = argumentRepository.findById(argumentId)
                    .orElseThrow(() -> new ArgumentNotFoundException("Argument not found with id: " + argumentId));

            FallacyCheck fallacyCheck = argument.getFallacyCheck();
            if (fallacyCheck == null) {
                fallacyCheck = new FallacyCheck();
                argument.setFallacyCheck(fallacyCheck);
            }

            if (fallacyTest.getScore() > 0.75) {
                fallacyCheck.setFallacyResult(FallacyResult.FALLACY);
                fallacyCheck.setExplanation(fallacyTest.getExplanation());
                fallacyCheck.setScore(fallacyTest.getScore());
            } else {
                fallacyCheck.setFallacyResult(FallacyResult.CLEAN);
                fallacyCheck.setScore(fallacyTest.getScore());
            }

            argumentRepository.save(argument);
        }

        return fallacyTest;
    }

    public AIArgumentResponseDto generateArgument(String text, String type, List<ArgumentResponseDto> debate) throws APIkeyNotFoundException, ServiceNotRespondingException {
        String debateString = formatDebateToJson(debate);
        String newArgumentText = debateGenerationClient.generateArgument(type,text, debateString);

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
            obj.put("segments", htmlParser.parseHtmlToSegments(arg.getText()));

            jsonArray.put(obj);
        }
        return jsonArray.toString(4);
    }

    @Transactional
    public ArgumentResponseDto syncWithTermit(long argumentId, String token) throws ArgumentNotFoundException, ServiceNotRespondingException {
        Argument argument = argumentRepository.findById(argumentId).orElseThrow(() -> new ArgumentNotFoundException("Argument with id: " + argumentId + " not found"));
        long debateId = argument.getDebate().getId();

        String htmlContent = termitClient.getArgumentContent(debateId, argumentId, token);
        List<TextSegment> segments = htmlParser.parseHtmlToSegments(htmlContent);
        for(TextSegment segment : segments){
            if(segment.getType().equals(TextSegmentType.TERM)){
                segment.setExplanation(termitClient.getDefinition(segment.getResource(), token));
            }
        }
        argument.setSegments(segments);

        argumentRepository.save(argument);
        return mapper.toDto(argument);
    }

    @Transactional
    public void saveArgumentTree(List<ArgumentResponseDto> dtos, Debate debate, User owner, JwtAuthenticationToken token) throws ServiceNotRespondingException {
        Map<Long, Argument> idMapping = new HashMap<>();
        List<ArgumentResponseDto> remainingArguments = new ArrayList<>(dtos);

        ArgumentResponseDto thesisDto = remainingArguments.stream()
                .filter(arg -> "THESIS".equals(arg.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Thesis not found in generated data"));

        Argument thesis = createArgumentEntity(thesisDto, null, debate, owner);
        Argument savedThesis = argumentRepository.save(thesis);
        termitClient.createArgumentFile(savedThesis.getText(), savedThesis.getDebate().getId(), savedThesis.getId(), token.getToken().getTokenValue());

        idMapping.put(thesisDto.getId(), savedThesis);
        remainingArguments.remove(thesisDto);

        while (!remainingArguments.isEmpty()) {
            List<ArgumentResponseDto> toRemove = new ArrayList<>();
            for (ArgumentResponseDto dto : remainingArguments) {
                if (idMapping.containsKey(dto.getParent())) {
                    Argument parentEntity = idMapping.get(dto.getParent());
                    Argument newArg = createArgumentEntity(dto, parentEntity, debate, owner);

                    Argument savedArg = argumentRepository.save(newArg);
                    termitClient.createArgumentFile(savedArg.getText(), savedArg.getDebate().getId(), savedArg.getId(), token.getToken().getTokenValue());
                    idMapping.put(dto.getId(), savedArg);
                    toRemove.add(dto);
                }
            }
            remainingArguments.removeAll(toRemove);
        }
    }

    private Argument createArgumentEntity(ArgumentResponseDto dto, Argument parent, Debate debate, User owner) {
        Argument argument = new Argument();
        argument.setText(dto.getText());
        argument.setType(ArgumentType.valueOf(dto.getType()));
        argument.setParent(parent);
        argument.setOwner(owner);
        argument.setDebate(debate);
        argument.setKialoId(dto.getId());
        argument.setKialoVersion(dto.getVersion());
        argument.setSegments(htmlParser.parseHtmlToSegments(dto.getText()));
        argument.setFallacyCheck(new FallacyCheck());
        return argument;
    }


    @Transactional
    public ArgumentResponseDto getArgument(Long argumentId, String keyCloakId) throws ArgumentNotFoundException, UserNotFoundException, UnauthorizedAccessException {
        Argument argument = argumentRepository.findById(argumentId).orElseThrow(() -> new ArgumentNotFoundException("Argument not found"));
        User user = userRepository.findByKeycloakId(keyCloakId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!argument.getOwner().getKeycloakId().equals(user.getKeycloakId())) {
            throw new UnauthorizedAccessException("You are not the owner of this argument");
        }

        return mapper.toDto(argument);
    }

    public void importArgument(NewArgumentDto dto, JwtAuthenticationToken token) throws DebateNotFoundException, ArgumentNotFoundException, UserNotFoundException, ServiceNotRespondingException {
        Debate debate = debateRepository.findDebateByKialoId(dto.getDebateId())
                .orElseThrow(() -> new DebateNotFoundException("Debate not found with Kialo ID: " + dto.getDebateId()));
        Argument parent = argumentRepository.findArgumentByKialoId(dto.getParentId())
                .orElseThrow(() -> new ArgumentNotFoundException("Argument not found with kialo id: " + dto.getParentId()));

        Long newArgumentId = this.createArgument(dto, token).getId();
        Argument newArgument = argumentRepository.findById(newArgumentId).orElseThrow(() -> new ArgumentNotFoundException("Argument not found with kialo id: " + newArgumentId));
        newArgument.setKialoVersion(dto.getVersion());
        newArgument.setKialoId(dto.getKialoId());
        argumentRepository.save(newArgument);
    }

    @Transactional
    public void deleteFallacy(long argumentId) throws ArgumentNotFoundException {
        Argument argument = argumentRepository.findById(argumentId)
                .orElseThrow(() -> new ArgumentNotFoundException("Argument not found with kialo id: " + argumentId));

        FallacyCheck fallacyCheck = argument.getFallacyCheck();
        fallacyCheck.setFallacy(null);
        fallacyCheck.setFallacyResult(FallacyResult.CLEAN);
        fallacyCheck.setExplanation(null);
        fallacyCheck.setExplanation(null);
    }


}