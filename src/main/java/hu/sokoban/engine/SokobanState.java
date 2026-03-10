package hu.sokoban.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class SokobanState {

    private final int rows;
    private final int cols;
    private final int playerRow;
    private final int playerCol;
    private final boolean[][] walls;
    private final boolean[][] goals;
    private final boolean[][] boxes;

    private SokobanState(Builder builder) {
        this.rows = builder.rows;
        this.cols = builder.cols;
        this.playerRow = builder.playerRow;
        this.playerCol = builder.playerCol;
        this.walls = deepCopy(builder.walls);
        this.goals = deepCopy(builder.goals);
        this.boxes = deepCopy(builder.boxes);
    }

    // --- Public API (csak ezeket latja a felhasznaloi heurisztika) ---

    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isWall(int r, int c) {
        return inBounds(r, c) && walls[r][c];
    }

    public boolean isGoal(int r, int c) {
        return inBounds(r, c) && goals[r][c];
    }

    public boolean isBox(int r, int c) {
        return inBounds(r, c) && boxes[r][c];
    }

    public int getBoxCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (boxes[r][c]) count++;
            }
        }
        return count;
    }

    public int getGoalCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (goals[r][c]) count++;
            }
        }
        return count;
    }

    public boolean isGoalState() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (goals[r][c] && !boxes[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<SokobanState> getSuccessors() {
        List<SokobanState> successors = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            SokobanState next = tryMove(dir);
            if (next != null) {
                successors.add(next);
            }
        }
        return successors;
    }

    // --- Belso logika ---

    private SokobanState tryMove(Direction dir) {
        int newRow = playerRow + dir.getDeltaRow();
        int newCol = playerCol + dir.getDeltaCol();

        if (!inBounds(newRow, newCol) || walls[newRow][newCol]) {
            return null;
        }

        boolean[][] newBoxes = deepCopy(this.boxes);

        if (boxes[newRow][newCol]) {
            int pushRow = newRow + dir.getDeltaRow();
            int pushCol = newCol + dir.getDeltaCol();

            if (!inBounds(pushRow, pushCol) || walls[pushRow][pushCol] || boxes[pushRow][pushCol]) {
                return null;
            }

            newBoxes[newRow][newCol] = false;
            newBoxes[pushRow][pushCol] = true;
        }

        return new Builder(rows, cols)
                .setWalls(this.walls)
                .setGoals(this.goals)
                .setBoxes(newBoxes)
                .setPlayer(newRow, newCol)
                .build();
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private static boolean[][] deepCopy(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    // --- Palya szovegbol parszolas (Factory Method) ---

    public static SokobanState fromString(String mapData) {
        String[] lines = mapData.split("\n", -1);
        int rows = lines.length;
        int cols = 0;
        for (String line : lines) {
            cols = Math.max(cols, line.length());
        }

        Builder builder = new Builder(rows, cols);

        for (int r = 0; r < rows; r++) {
            String line = lines[r];
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                switch (ch) {
                    case '#' -> builder.setWall(r, c);
                    case '@' -> builder.setPlayer(r, c);
                    case '+' -> {
                        builder.setPlayer(r, c);
                        builder.setGoal(r, c);
                    }
                    case '$' -> builder.setBox(r, c);
                    case '*' -> {
                        builder.setBox(r, c);
                        builder.setGoal(r, c);
                    }
                    case '.' -> builder.setGoal(r, c);
                    default -> { /* ures mezo vagy szokoz */ }
                }
            }
        }

        return builder.build();
    }

    // --- equals / hashCode (A* closed halmazhoz) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SokobanState other)) return false;
        return playerRow == other.playerRow
                && playerCol == other.playerCol
                && Arrays.deepEquals(boxes, other.boxes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(playerRow, playerCol);
        result = 31 * result + Arrays.deepHashCode(boxes);
        return result;
    }

    // --- Builder pattern ---

    public static class Builder {
        private final int rows;
        private final int cols;
        private int playerRow;
        private int playerCol;
        private boolean[][] walls;
        private boolean[][] goals;
        private boolean[][] boxes;

        public Builder(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            this.walls = new boolean[rows][cols];
            this.goals = new boolean[rows][cols];
            this.boxes = new boolean[rows][cols];
        }

        public Builder setPlayer(int r, int c) {
            this.playerRow = r;
            this.playerCol = c;
            return this;
        }

        public Builder setWall(int r, int c) {
            this.walls[r][c] = true;
            return this;
        }

        public Builder setGoal(int r, int c) {
            this.goals[r][c] = true;
            return this;
        }

        public Builder setBox(int r, int c) {
            this.boxes[r][c] = true;
            return this;
        }

        public Builder setWalls(boolean[][] walls) {
            this.walls = deepCopy(walls);
            return this;
        }

        public Builder setGoals(boolean[][] goals) {
            this.goals = deepCopy(goals);
            return this;
        }

        public Builder setBoxes(boolean[][] boxes) {
            this.boxes = deepCopy(boxes);
            return this;
        }

        public SokobanState build() {
            return new SokobanState(this);
        }
    }
}
