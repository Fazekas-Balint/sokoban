package hu.sokoban.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_results")
public class EvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heuristic_id", nullable = false)
    private Heuristic heuristic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id", nullable = false)
    private SokobanMap sokobanMap;

    @Column(nullable = false)
    private boolean solved;

    @Column(nullable = false)
    private boolean timedOut;

    @Column(nullable = false)
    private int nodesExpanded;

    private int solutionLength;

    @Column(nullable = false)
    private long executionTimeMs;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    private double score;

    protected EvaluationResult() {}

    public EvaluationResult(Heuristic heuristic, SokobanMap sokobanMap) {
        this.heuristic = heuristic;
        this.sokobanMap = sokobanMap;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public Heuristic getHeuristic() { return heuristic; }
    public SokobanMap getSokobanMap() { return sokobanMap; }

    public boolean isSolved() { return solved; }
    public void setSolved(boolean solved) { this.solved = solved; }

    public boolean isTimedOut() { return timedOut; }
    public void setTimedOut(boolean timedOut) { this.timedOut = timedOut; }

    public int getNodesExpanded() { return nodesExpanded; }
    public void setNodesExpanded(int nodesExpanded) { this.nodesExpanded = nodesExpanded; }

    public int getSolutionLength() { return solutionLength; }
    public void setSolutionLength(int solutionLength) { this.solutionLength = solutionLength; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
