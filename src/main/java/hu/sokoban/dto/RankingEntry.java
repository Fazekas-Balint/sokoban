package hu.sokoban.dto;

public class RankingEntry {

    private int rank;
    private String username;
    private String heuristicName;
    private Long heuristicId;
    private double totalScore;
    private int solvedCount;
    private int totalMaps;
    private double avgNodesExpanded;

    public RankingEntry(int rank, String username, String heuristicName, Long heuristicId,
                        double totalScore, int solvedCount, int totalMaps, double avgNodesExpanded) {
        this.rank = rank;
        this.username = username;
        this.heuristicName = heuristicName;
        this.heuristicId = heuristicId;
        this.totalScore = totalScore;
        this.solvedCount = solvedCount;
        this.totalMaps = totalMaps;
        this.avgNodesExpanded = avgNodesExpanded;
    }

    public int getRank() { return rank; }
    public String getUsername() { return username; }
    public String getHeuristicName() { return heuristicName; }
    public Long getHeuristicId() { return heuristicId; }
    public double getTotalScore() { return totalScore; }
    public int getSolvedCount() { return solvedCount; }
    public int getTotalMaps() { return totalMaps; }
    public double getAvgNodesExpanded() { return avgNodesExpanded; }
}
