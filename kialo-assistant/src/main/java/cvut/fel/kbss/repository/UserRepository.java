package cvut.fel.kbss.repository;

import cvut.fel.kbss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByUsername(String username);
    Optional<User> findByKeycloakId(String keycloakId);
}
