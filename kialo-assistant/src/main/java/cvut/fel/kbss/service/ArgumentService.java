package cvut.fel.kbss.service;


import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.ArgumentType;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import cvut.fel.kbss.repository.ArgumentRepository;
import cvut.fel.kbss.repository.DebateRepository;
import cvut.fel.kbss.repository.UserRepository;
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
    public ArgumentResponseDto createArgument(String text, ArgumentType type, Long parentId, Long debateId, Long userId){
        Optional<User> ownerOpt = userRepository.findById(userId.toString());
        if(ownerOpt.isEmpty()){
            return null;
        }
        Optional<Argument> parentOpt = argumentRepository.findById(parentId.toString());
        if(parentOpt.isEmpty()){
            return null;
        }
        Optional<Debate> debateOpt = debateRepository.findById(debateId.toString());
        if(debateOpt.isEmpty()){
            return null;
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

        ArgumentResponseDto response = mapper.toDto(argument);
        return response;
    }
}
