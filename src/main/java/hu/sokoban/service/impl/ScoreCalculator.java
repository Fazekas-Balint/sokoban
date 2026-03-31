package hu.sokoban.service.impl;

import hu.sokoban.model.EvaluationResult;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    private static final double BASE_SCORE = 100.0;
    private static final double NODE_BONUS_MAX = 50.0;
    private static final double NODE_BONUS_FACTOR = 5.0;
    private static final double OPTIMALITY_BONUS = 20.0;
    private static final double SPEED_BONUS_MAX = 20.0;
    private static final double SPEED_DIVISOR = 250.0;

    public double calculate(EvaluationResult result, int bestSolutionLength) {
        if (!result.isSolved()) {
            return 0.0;
        }

        double score = BASE_SCORE;

        score += nodeBonus(result.getNodesExpanded());

        if (bestSolutionLength > 0 && result.getSolutionLength() == bestSolutionLength) {
            score += OPTIMALITY_BONUS;
        }

        score += speedBonus(result.getExecutionTimeMs());

        return Math.max(0, score);
    }

    private double nodeBonus(int nodesExpanded) {
        if (nodesExpanded <= 0) return NODE_BONUS_MAX;
        return Math.max(0, NODE_BONUS_MAX - (Math.log(nodesExpanded) / Math.log(2)) * NODE_BONUS_FACTOR);
    }

    private double speedBonus(long executionTimeMs) {
        return Math.max(0, SPEED_BONUS_MAX - (executionTimeMs / SPEED_DIVISOR));
    }
}
