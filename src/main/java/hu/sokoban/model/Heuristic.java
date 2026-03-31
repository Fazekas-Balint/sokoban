package hu.sokoban.model;

import hu.sokoban.model.enums.HeuristicStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "heuristics")
public class Heuristic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeuristicStatus status = HeuristicStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String compilationError;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "heuristic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationResult> evaluationResults = new ArrayList<>();

    protected Heuristic() {}

    public Heuristic(String name, String sourceCode, User author) {
        this.name = name;
        this.sourceCode = sourceCode;
        this.author = author;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public User getAuthor() { return author; }

    public HeuristicStatus getStatus() { return status; }
    public void setStatus(HeuristicStatus status) { this.status = status; }

    public String getCompilationError() { return compilationError; }
    public void setCompilationError(String compilationError) { this.compilationError = compilationError; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<EvaluationResult> getEvaluationResults() { return evaluationResults; }

    public void markCompiled() {
        this.status = HeuristicStatus.COMPILED;
        this.compilationError = null;
    }

    public void markError(String error) {
        this.status = HeuristicStatus.ERROR;
        this.compilationError = error;
    }
}
