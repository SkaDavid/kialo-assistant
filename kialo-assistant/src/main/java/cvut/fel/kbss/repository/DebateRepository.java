package cvut.fel.kbss.repository;

import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebateRepository extends JpaRepository<Debate, Long> {
    List<Debate> findByTopic(String title);

    @Query("SELECT d FROM Debate d WHERE d.visibility = 'PUBLIC' OR d.owner.keycloakId = :userKeycloakId")
    List<Debate> findDebatesForUser(@Param("userKeycloakId") String userKeycloakId);

    Debate findDebateByKialoId(Long kialoId);
}

