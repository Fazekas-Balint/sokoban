package hu.sokoban.service.impl;

import hu.sokoban.engine.AStarSolver;
import hu.sokoban.engine.HeuristicFunction;
import hu.sokoban.engine.SokobanState;
import hu.sokoban.engine.SolutionResult;
import hu.sokoban.model.EvaluationResult;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.SokobanMap;
import hu.sokoban.repository.EvaluationResultRepository;
import hu.sokoban.service.EvaluationService;
import hu.sokoban.service.HeuristicCompilerService;
import hu.sokoban.service.HeuristicService;
import hu.sokoban.service.MapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    private final HeuristicService heuristicService;
    private final MapService mapService;
    private final HeuristicCompilerService compilerService;
    private final EvaluationResultRepository resultRepository;
    private final ScoreCalculator scoreCalculator;

    @Value("${sokoban.evaluation.max-nodes:100000}")
    private int maxNodes;

    @Value("${sokoban.evaluation.time-limit-ms:5000}")
    private long timeLimitMs;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EvaluationServiceImpl(HeuristicService heuristicService,
                                 MapService mapService,
                                 HeuristicCompilerService compilerService,
                                 EvaluationResultRepository resultRepository,
                                 ScoreCalculator scoreCalculator) {
        this.heuristicService = heuristicService;
        this.mapService = mapService;
        this.compilerService = compilerService;
        this.resultRepository = resultRepository;
        this.scoreCalculator = scoreCalculator;
    }

    @Override
    @Scheduled(cron = "${sokoban.evaluation.cron:0 0 3 * * *}")
    public void evaluateAll() {
        log.info("Automatikus kiertekeles indul...");

        List<Heuristic> heuristics = heuristicService.findAllCompiled();
        List<SokobanMap> maps = mapService.findBenchmarkMaps();

        if (heuristics.isEmpty() || maps.isEmpty()) {
            log.info("Nincs kiertekelheto heurisztika vagy benchmark palya");
            return;
        }

        for (Heuristic heuristic : heuristics) {
            // Ujraforditas, ha a memoriaban nincs meg a leforditott osztaly
            try {
                compilerService.loadCompiled(heuristic.getId());
            } catch (IllegalStateException e) {
                log.info("Heurisztika ujraforditasa szukseges: {} (id={})", heuristic.getName(), heuristic.getId());
                var result = compilerService.compile(heuristic.getId(), heuristic.getSourceCode());
                if (!result.success()) {
                    log.error("Ujraforditas sikertelen - heurisztika: {} - {}", heuristic.getId(), result.errorMessage());
                    continue;
                }
            }

            for (SokobanMap map : maps) {
                try {
                    SolutionResult solution = evaluateSingle(heuristic, map);
                    saveResult(heuristic, map, solution);
                } catch (Exception e) {
                    log.error("Hiba a kiertekelesnel - heurisztika: {}, palya: {}",
                            heuristic.getId(), map.getId(), e);
                }
            }
        }

        calculateScores(maps);
        log.info("Kiertekeles befejezve: {} heurisztika x {} palya", heuristics.size(), maps.size());
    }

    @Override
    public SolutionResult evaluateSingle(Heuristic heuristic, SokobanMap map) {
        HeuristicFunction function = compilerService.loadCompiled(heuristic.getId());
        SokobanState initialState = SokobanState.fromString(map.getMapData());
        AStarSolver solver = new AStarSolver(function, maxNodes, timeLimitMs);

        Future<SolutionResult> future = executor.submit(() -> solver.solve(initialState));

        try {
            return future.get(timeLimitMs + 1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return SolutionResult.timeout(0, timeLimitMs);
        } catch (Exception e) {
            log.error("Hiba a heurisztika futtatasa kozben", e);
            return SolutionResult.failure(0, 0);
        }
    }

    private void saveResult(Heuristic heuristic, SokobanMap map, SolutionResult solution) {
        Optional<EvaluationResult> existing = resultRepository.findByHeuristicAndSokobanMap(heuristic, map);
        EvaluationResult result = existing.orElseGet(() -> new EvaluationResult(heuristic, map));

        result.setSolved(solution.isSolved());
        result.setTimedOut(solution.isTimedOut());
        result.setNodesExpanded(solution.getNodesExpanded());
        result.setSolutionLength(solution.getSolutionLength());
        result.setExecutionTimeMs(solution.getExecutionTimeMs());

        resultRepository.save(result);
    }

    private void calculateScores(List<SokobanMap> maps) {
        for (SokobanMap map : maps) {
            Optional<Integer> bestLength = resultRepository.findBestSolutionLengthForMap(map);
            List<EvaluationResult> results = resultRepository.findBySokobanMap(map);

            for (EvaluationResult result : results) {
                double score = scoreCalculator.calculate(result, bestLength.orElse(0));
                result.setScore(score);
                resultRepository.save(result);
            }
        }
    }
}
