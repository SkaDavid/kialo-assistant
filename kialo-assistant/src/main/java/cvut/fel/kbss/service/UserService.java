package cvut.fel.kbss.service;

import cvut.fel.kbss.model.User;
import cvut.fel.kbss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional
    public String persistUser(String username){
        System.out.println(username);
        User user = new User();
        user.setUsername(username);
        userRepository.save(user);
        return username;
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
