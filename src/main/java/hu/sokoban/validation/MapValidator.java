package hu.sokoban.validation;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MapValidator {

    private static final Set<Character> VALID_CHARS = Set.of('#', ' ', '@', '+', '$', '*', '.');

    public void validate(String mapData) {
        if (mapData == null || mapData.isBlank()) {
            throw new MapValidationException("A palya nem lehet ures");
        }

        String[] lines = mapData.split("\n", -1);

        // Ures sorok eltavolitasa a vegerol
        int lastNonEmpty = lines.length - 1;
        while (lastNonEmpty >= 0 && lines[lastNonEmpty].isBlank()) {
            lastNonEmpty--;
        }
        if (lastNonEmpty < 0) {
            throw new MapValidationException("A palya nem lehet ures");
        }

        int playerCount = 0;
        int boxCount = 0;
        int goalCount = 0;

        for (int r = 0; r <= lastNonEmpty; r++) {
            String line = lines[r];
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);

                if (!VALID_CHARS.contains(ch)) {
                    throw new MapValidationException(
                            "Ervenytelen karakter a(z) %d. sor %d. oszlopaban: '%c'".formatted(r + 1, c + 1, ch));
                }

                switch (ch) {
                    case '@' -> playerCount++;
                    case '+' -> {
                        playerCount++;
                        goalCount++;
                    }
                    case '$' -> boxCount++;
                    case '*' -> {
                        boxCount++;
                        goalCount++;
                    }
                    case '.' -> goalCount++;
                }
            }
        }

        if (playerCount == 0) {
            throw new MapValidationException("Nincs jatekos a palyan (hasznalj '@' vagy '+' karaktert)");
        }
        if (playerCount > 1) {
            throw new MapValidationException("Tobb jatekos van a palyan (" + playerCount + " db). Pontosan egy kell");
        }
        if (boxCount == 0) {
            throw new MapValidationException("Nincs doboz a palyan (hasznalj '$' vagy '*' karaktert)");
        }
        if (goalCount == 0) {
            throw new MapValidationException("Nincs celmezo a palyan (hasznalj '.' vagy '*' vagy '+' karaktert)");
        }
        if (boxCount != goalCount) {
            throw new MapValidationException(
                    "A dobozok szama (%d) nem egyezik a celmezo szamaval (%d)".formatted(boxCount, goalCount));
        }
    }

    public static class MapValidationException extends RuntimeException {
        public MapValidationException(String message) {
            super(message);
        }
    }
}
