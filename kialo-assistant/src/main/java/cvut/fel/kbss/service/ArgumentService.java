package cvut.fel.kbss.service;


import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.exception.ArgumentNotFoundException;
import cvut.fel.kbss.exception.DebateNotFoundException;
import cvut.fel.kbss.exception.UnauthorizedAccessException;
import cvut.fel.kbss.exception.UserNotFoundException;
import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.ArgumentType;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final Mapper mapper;

    @Autowired
    public ArgumentService(ArgumentRepository argumentRepository, UserRepository userRepository, DebateRepository debateRepository, Mapper mapper){
        this.argumentRepository = argumentRepository;
        this.userRepository = userRepository;
        this.debateRepository = debateRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ArgumentResponseDto createArgument(String text, ArgumentType type, Long parentId, Long debateId, Long userId) throws UserNotFoundException, DebateNotFoundException, ArgumentNotFoundException {
        Optional<User> ownerOpt = userRepository.findById(userId);
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

        if(!argument.getOwner().getId().equals(user.getId())){
            throw new UnauthorizedAccessException("User is not the owner of the argument");
        }
        argumentRepository.deleteById(argumentId);
    }
}
