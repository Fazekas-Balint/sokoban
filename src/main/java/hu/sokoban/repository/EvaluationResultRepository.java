package hu.sokoban.repository;

import hu.sokoban.model.EvaluationResult;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.SokobanMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {

    List<EvaluationResult> findByHeuristicOrderByEvaluatedAtDesc(Heuristic heuristic);

    List<EvaluationResult> findBySokobanMap(SokobanMap map);

    Optional<EvaluationResult> findByHeuristicAndSokobanMap(Heuristic heuristic, SokobanMap map);

    @Query("SELECT MIN(e.solutionLength) FROM EvaluationResult e " +
           "WHERE e.sokobanMap = :map AND e.solved = true AND e.solutionLength > 0")
    Optional<Integer> findBestSolutionLengthForMap(@Param("map") SokobanMap map);

    void deleteByHeuristic(Heuristic heuristic);
}
