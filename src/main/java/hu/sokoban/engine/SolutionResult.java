package hu.sokoban.engine;

import java.util.Collections;
import java.util.List;

public final class SolutionResult {

    private final boolean solved;
    private final boolean timedOut;
    private final int nodesExpanded;
    private final int solutionLength;
    private final long executionTimeMs;
    private final List<SokobanState> path;

    private SolutionResult(boolean solved, boolean timedOut, int nodesExpanded,
                           int solutionLength, long executionTimeMs, List<SokobanState> path) {
        this.solved = solved;
        this.timedOut = timedOut;
        this.nodesExpanded = nodesExpanded;
        this.solutionLength = solutionLength;
        this.executionTimeMs = executionTimeMs;
        this.path = path != null ? Collections.unmodifiableList(path) : Collections.emptyList();
    }

    public static SolutionResult success(int nodesExpanded, int solutionLength,
                                         long executionTimeMs, List<SokobanState> path) {
        return new SolutionResult(true, false, nodesExpanded, solutionLength, executionTimeMs, path);
    }

    public static SolutionResult timeout(int nodesExpanded, long executionTimeMs) {
        return new SolutionResult(false, true, nodesExpanded, 0, executionTimeMs, null);
    }

    public static SolutionResult failure(int nodesExpanded, long executionTimeMs) {
        return new SolutionResult(false, false, nodesExpanded, 0, executionTimeMs, null);
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public int getNodesExpanded() {
        return nodesExpanded;
    }

    public int getSolutionLength() {
        return solutionLength;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public List<SokobanState> getPath() {
        return path;
    }
}
