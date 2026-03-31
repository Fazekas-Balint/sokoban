package hu.sokoban.model;

import hu.sokoban.model.enums.Difficulty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sokoban_maps")
public class SokobanMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mapData;

    @Column(nullable = false)
    private int rows;

    @Column(nullable = false)
    private int cols;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private boolean benchmark = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sokobanMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationResult> evaluationResults = new ArrayList<>();

    protected SokobanMap() {}

    public SokobanMap(String name, String mapData, int rows, int cols, User uploadedBy) {
        this.name = name;
        this.mapData = mapData;
        this.rows = rows;
        this.cols = cols;
        this.uploadedBy = uploadedBy;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMapData() { return mapData; }
    public void setMapData(String mapData) { this.mapData = mapData; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public boolean isBenchmark() { return benchmark; }
    public void setBenchmark(boolean benchmark) { this.benchmark = benchmark; }

    public User getUploadedBy() { return uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public List<EvaluationResult> getEvaluationResults() { return evaluationResults; }
}
