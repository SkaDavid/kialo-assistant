package cvut.fel.kbss.repository;

import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebateRepository extends JpaRepository<Debate, Long> {
    List<Debate> findByTitle(String title);
}

