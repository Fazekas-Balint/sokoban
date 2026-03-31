package hu.sokoban.repository;

import hu.sokoban.model.Heuristic;
import hu.sokoban.model.User;
import hu.sokoban.model.enums.HeuristicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeuristicRepository extends JpaRepository<Heuristic, Long> {

    List<Heuristic> findByAuthorOrderByCreatedAtDesc(User author);

    List<Heuristic> findByStatus(HeuristicStatus status);

    List<Heuristic> findAllByOrderByCreatedAtDesc();
}
