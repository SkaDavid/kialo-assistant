package cvut.fel.kbss.repository;

import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArgumentRepository extends JpaRepository<Argument, Long> {

    Optional<Argument> findArgumentByKialoId(Long parentId);
}

