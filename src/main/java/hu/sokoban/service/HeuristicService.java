package hu.sokoban.service;

import hu.sokoban.dto.HeuristicDto;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.User;
import java.util.List;
import java.util.Optional;

public interface HeuristicService {

    Heuristic create(HeuristicDto dto, User author);

    Heuristic update(Long id, HeuristicDto dto);

    Optional<Heuristic> findById(Long id);

    List<Heuristic> findByUser(User user);

    List<Heuristic> findAllCompiled();

    void delete(Long id);
}
