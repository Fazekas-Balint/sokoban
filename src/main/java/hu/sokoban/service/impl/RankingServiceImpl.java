package hu.sokoban.service.impl;

import hu.sokoban.dto.RankingEntry;
import hu.sokoban.model.EvaluationResult;
import hu.sokoban.model.Heuristic;
import hu.sokoban.repository.EvaluationResultRepository;
import hu.sokoban.service.HeuristicService;
import hu.sokoban.service.MapService;
import hu.sokoban.service.RankingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final HeuristicService heuristicService;
    private final MapService mapService;
    private final EvaluationResultRepository resultRepository;

    public RankingServiceImpl(HeuristicService heuristicService,
                              MapService mapService,
                              EvaluationResultRepository resultRepository) {
        this.heuristicService = heuristicService;
        this.mapService = mapService;
        this.resultRepository = resultRepository;
    }

    @Override
    public List<RankingEntry> calculateRanking() {
        List<Heuristic> heuristics = heuristicService.findAllCompiled();
        int totalMaps = mapService.findBenchmarkMaps().size();

        List<RankingEntry> entries = new ArrayList<>();

        for (Heuristic heuristic : heuristics) {
            List<EvaluationResult> results = resultRepository.findByHeuristicOrderByEvaluatedAtDesc(heuristic);

            if (results.isEmpty()) continue;

            double totalScore = results.stream().mapToDouble(EvaluationResult::getScore).sum();
            int solvedCount = (int) results.stream().filter(EvaluationResult::isSolved).count();
            double avgNodes = results.stream()
                    .filter(EvaluationResult::isSolved)
                    .mapToInt(EvaluationResult::getNodesExpanded)
                    .average()
                    .orElse(0);

            entries.add(new RankingEntry(
                    0,
                    heuristic.getAuthor().getUsername(),
                    heuristic.getName(),
                    heuristic.getId(),
                    totalScore,
                    solvedCount,
                    totalMaps,
                    avgNodes
            ));
        }

        entries.sort(Comparator.comparingDouble(RankingEntry::getTotalScore).reversed());

        List<RankingEntry> ranked = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            RankingEntry e = entries.get(i);
            ranked.add(new RankingEntry(
                    i + 1,
                    e.getUsername(),
                    e.getHeuristicName(),
                    e.getHeuristicId(),
                    e.getTotalScore(),
                    e.getSolvedCount(),
                    e.getTotalMaps(),
                    e.getAvgNodesExpanded()
            ));
        }

        return ranked;
    }
}
