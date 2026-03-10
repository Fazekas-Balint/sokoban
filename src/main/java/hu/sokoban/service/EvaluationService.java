package hu.sokoban.service;

import hu.sokoban.engine.SolutionResult;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.SokobanMap;

public interface EvaluationService {

    void evaluateAll();

    SolutionResult evaluateSingle(Heuristic heuristic, SokobanMap map);
}
