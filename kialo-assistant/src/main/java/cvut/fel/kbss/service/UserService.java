package cvut.fel.kbss.service;

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

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional
    public String persistUser(String username, String id){
        System.out.println(username);
        User user = new User();
        user.setUsername(username);
        user.setKeycloakId(id);
        userRepository.save(user);
        return username;
    }

    @Transactional
    public void syncUser(String username, String keyCloakId){
        Optional<User> userOpt = userRepository.findByKeycloakId(keyCloakId);
        if(userOpt.isEmpty()){
            persistUser(username, keyCloakId);
        }
    }


    @Transactional
    public User findUser(String username){
        List<User> users = this.userRepository.findByUsername(username);
        if(users.isEmpty()){
            users = this.userRepository.findAll();
            if(users.isEmpty()){
                return new User();
            }
            return users.getFirst();
        }
        return users.getFirst();
    }
}
