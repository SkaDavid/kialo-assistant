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
import cvut.fel.kbss.model.*;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        argument.setSegments(this.parseHtmlToSegments(text));

        List<Argument> debateArguments = debate.getArguments();
        debateArguments.add(argument);
        debate.setArguments(debateArguments);
        Argument newArgument = argumentRepository.save(argument);

        termitClient.createArgumentFile(text, debateId, newArgument.getId(), token.getToken().getTokenValue());

        return mapper.toDto(newArgument);
    }

    @Transactional
    public void deleteArgument(Long argumentId, JwtAuthenticationToken token) throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException, ServiceNotRespondingException {
        Optional<Argument> argumentOpt = argumentRepository.findById(argumentId);
        Optional<User> userOpt = userRepository.findByKeycloakId(token.getToken().getSubject());
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

        long debateId = argument.getDebate().getId();
        termitClient.deleteArgumentFile(debateId, argumentId, token.getToken().getTokenValue());
        argumentRepository.deleteById(argumentId);
    }

    @Transactional
    public ArgumentResponseDto updateArgument(long argumentId, String newText, ArgumentType newType, JwtAuthenticationToken token)
            throws ArgumentNotFoundException, UnauthorizedAccessException, UserNotFoundException, ServiceNotRespondingException {

        Optional<Argument> argumentOpt = argumentRepository.findById(argumentId);
        Optional<User> userOpt = userRepository.findByKeycloakId(token.getToken().getSubject());
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
        argument.setSegments(this.parseHtmlToSegments(newText));

        long debateId = argument.getDebate().getId();
        termitClient.updateArgumentFile(debateId, argumentId, newText, token.getToken().getTokenValue());
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
            obj.put("segments", this.parseHtmlToSegments(arg.getText()));

            jsonArray.put(obj);
        }
        return jsonArray.toString(4);
    }

    @Transactional
    public ArgumentResponseDto syncWithTermit(long argumentId, String token) throws ArgumentNotFoundException, ServiceNotRespondingException {
        Optional<Argument> argumentOpt = argumentRepository.findById(argumentId);
        if(argumentOpt.isEmpty()){
            throw new ArgumentNotFoundException("Argument with id: " + argumentId + " not found");
        }
        Argument argument = argumentOpt.get();
        long debateId = argument.getDebate().getId();

        String htmlContent = termitClient.getArgumentContent(debateId, argumentId, token);
        List<TextSegment> segments = parseHtmlToSegments(htmlContent);
        for(TextSegment segment : segments){
            if(segment.getType().equals(TextSegmentType.TERM)){
                segment.setExplanation(termitClient.findDefinition(segment.getResource(), token));
            }
        }
        argument.setSegments(segments);

        argumentRepository.save(argument);
        return mapper.toDto(argument);
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
}