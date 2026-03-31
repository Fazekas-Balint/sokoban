package hu.sokoban.engine;

import java.util.*;

public class AStarSolver {

    private final HeuristicFunction heuristic;
    private final int maxNodes;
    private final long timeLimitMs;

    public AStarSolver(HeuristicFunction heuristic, int maxNodes, long timeLimitMs) {
        this.heuristic = Objects.requireNonNull(heuristic, "heuristic cannot be null");
        this.maxNodes = maxNodes;
        this.timeLimitMs = timeLimitMs;
    }

    public SolutionResult solve(SokobanState initialState) {
        long startTime = System.currentTimeMillis();
        int nodesExpanded = 0;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        Map<SokobanState, Integer> bestG = new HashMap<>();

        int initialH = safeHeur(initialState);
        if (initialH == Integer.MAX_VALUE) {
            return SolutionResult.failure(0, elapsed(startTime));
        }
        Node startNode = new Node(initialState, null, 0, initialH);
        open.add(startNode);
        bestG.put(initialState, 0);

        while (!open.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                return SolutionResult.timeout(nodesExpanded, elapsed(startTime));
            }
            if (elapsed(startTime) > timeLimitMs) {
                return SolutionResult.timeout(nodesExpanded, elapsed(startTime));
            }
            if (nodesExpanded >= maxNodes) {
                return SolutionResult.timeout(nodesExpanded, elapsed(startTime));
            }

            Node current = open.poll();

            if (current.getState().isGoalState()) {
                List<SokobanState> path = reconstructPath(current);
                return SolutionResult.success(nodesExpanded, path.size() - 1, elapsed(startTime), path);
            }

            nodesExpanded++;

            for (SokobanState successor : current.getState().getSuccessors()) {
                int tentativeG = current.getG() + 1;

                Integer previousG = bestG.get(successor);
                if (previousG != null && tentativeG >= previousG) {
                    continue;
                }

                bestG.put(successor, tentativeG);
                int h = safeHeur(successor);
                if (h == Integer.MAX_VALUE) {
                    return SolutionResult.failure(nodesExpanded, elapsed(startTime));
                }
                open.add(new Node(successor, current, tentativeG, h));
            }
        }

        return SolutionResult.failure(nodesExpanded, elapsed(startTime));
    }

    /**
     * Biztonsagos heurisztika hivas: elkapja a felhasznaloi kod kivetelet,
     * hogy egy hibas/rosszindulatu heurisztika ne omlessze az egesz solvert.
     */
    private int safeHeur(SokobanState state) {
        try {
            int result = heuristic.heur(state);
            return Math.max(0, result);
        } catch (Exception | StackOverflowError | OutOfMemoryError e) {
            return Integer.MAX_VALUE;
        }
    }

    private long elapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private List<SokobanState> reconstructPath(Node goalNode) {
        List<SokobanState> path = new ArrayList<>();
        Node current = goalNode;
        while (current != null) {
            path.add(current.getState());
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    // --- Belso Node osztaly az A* grafjaban ---

    private static class Node {
        private final SokobanState state;
        private final Node parent;
        private final int g;
        private final int h;

        Node(SokobanState state, Node parent, int g, int h) {
            this.state = state;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        SokobanState getState() {
            return state;
        }

        Node getParent() {
            return parent;
        }

        int getG() {
            return g;
        }

        int getF() {
            return g + h;
        }
    }
}
