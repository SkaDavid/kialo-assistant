package cvut.fel.kbss.service;

import cvut.fel.kbss.dao.UserDao;
import cvut.fel.kbss.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserService {
    private UserDao userDao;

    @Autowired
    public UserService(UserDao userDao){
        this.userDao = userDao;
    }

    @Transactional
    public String persistUser(String username){
        User user = new User();
        user.setUsername(username);
        userDao.save(user);
        return username;
    }


    @Transactional
    public User findUser(String username){
        List<User> users = this.userDao.findByName(username);
        return users.getFirst();
    }

}
