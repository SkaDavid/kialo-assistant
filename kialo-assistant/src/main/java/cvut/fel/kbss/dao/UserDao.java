package cvut.fel.kbss.dao;

import cvut.fel.kbss.model.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private EntityManager em;

    public void save(User user) {
        em.persist(user);
    }

    public User find(URI id) {
        return em.find(User.class, id);
    }

    public List<User> findByName(String name) {
        return em.createQuery("SELECT u FROM User u WHERE u.username = :jmeno", User.class)
                .setParameter("jmeno", name)
                .getResultList();
    }
}