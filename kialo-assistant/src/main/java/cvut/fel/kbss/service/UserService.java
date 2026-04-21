package cvut.fel.kbss.service;

import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.UserResponseDto;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.UserNotFoundException;
import cvut.fel.kbss.model.User;
import cvut.fel.kbss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Autowired
    public UserService(UserRepository userRepository, Mapper mapper){
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public UserResponseDto persistUser(String username, String id){
        System.out.println(username);
        User user = new User();
        user.setUsername(username);
        user.setKeycloakId(id);
        User newUser = userRepository.save(user);
        return mapper.toDto(newUser);
    }

    @Transactional
    public void syncUser(String username, String keyCloakId){
        Optional<User> userOpt = userRepository.findByKeycloakId(keyCloakId);
        if(userOpt.isEmpty()){
            persistUser(username, keyCloakId);
        }
    }


    @Transactional
    public UserResponseDto findUser(String username) throws UserNotFoundException {
        List<User> users = this.userRepository.findByUsername(username);
        if(users.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        return mapper.toDto(users.getFirst());
    }
}
