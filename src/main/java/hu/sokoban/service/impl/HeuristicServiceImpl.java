package hu.sokoban.service.impl;

import hu.sokoban.dto.HeuristicDto;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.User;
import hu.sokoban.model.enums.HeuristicStatus;
import hu.sokoban.repository.EvaluationResultRepository;
import hu.sokoban.repository.HeuristicRepository;
import hu.sokoban.service.HeuristicCompilerService;
import hu.sokoban.service.HeuristicCompilerService.CompilationResult;
import hu.sokoban.service.HeuristicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HeuristicServiceImpl implements HeuristicService {

    private final HeuristicRepository heuristicRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final HeuristicCompilerService compilerService;

    public HeuristicServiceImpl(HeuristicRepository heuristicRepository,
                                EvaluationResultRepository evaluationResultRepository,
                                HeuristicCompilerService compilerService) {
        this.heuristicRepository = heuristicRepository;
        this.evaluationResultRepository = evaluationResultRepository;
        this.compilerService = compilerService;
    }

    @Override
    public Heuristic create(HeuristicDto dto, User author) {
        Heuristic heuristic = new Heuristic(dto.getName(), dto.getSourceCode(), author);
        heuristic = heuristicRepository.save(heuristic);

        compileAndUpdateStatus(heuristic);
        return heuristic;
    }

    @Override
    public Heuristic update(Long id, HeuristicDto dto) {
        Heuristic heuristic = heuristicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Heurisztika nem talalhato: " + id));

        heuristic.setName(dto.getName());
        heuristic.setSourceCode(dto.getSourceCode());

        evaluationResultRepository.deleteByHeuristic(heuristic);

        compileAndUpdateStatus(heuristic);
        return heuristicRepository.save(heuristic);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Heuristic> findById(Long id) {
        return heuristicRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Heuristic> findByUser(User user) {
        return heuristicRepository.findByAuthorOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Heuristic> findAllCompiled() {
        return heuristicRepository.findByStatus(HeuristicStatus.COMPILED);
    }

    @Override
    public void delete(Long id) {
        heuristicRepository.deleteById(id);
    }

    private void compileAndUpdateStatus(Heuristic heuristic) {
        CompilationResult result = compilerService.compile(heuristic.getId(), heuristic.getSourceCode());
        if (result.success()) {
            heuristic.markCompiled();
        } else {
            heuristic.markError(result.errorMessage());
        }
    }
}
