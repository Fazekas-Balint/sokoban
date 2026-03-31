# Teszt adatok

## Pályák

### 1. Minimal (1 doboz)
```
#####
#   #
# $ #
# .@#
#####
```

### 2. Kettos (2 doboz)
```
######
#    #
# $$ #
# .. #
#   @#
######
```

### 3. L-alaku
```
#####
#   ##
# $  #
##$  #
 #. .#
 # @ #
 #####
```

### 4. Folyoso
```
########
#      #
# $@$. #
#   .  #
########
```

### 5. Sarok
```
######
#    #
# #$ #
# .  #
#  $.#
#  @ #
######
```

### 6. Szoba
```
  ####
###  #
# $ .#
#  $.#
# @  #
######
```

### 7. Cikkckakk
```
#######
#  .  #
# #$# #
#  $  #
# #.# #
#  @  #
#######
```

### 8. Harom doboz
```
########
#      #
# $ $  #
# .#.  #
#  $  @#
#  .   #
########
```

### 9. Labirintus
```
#########
#   #   #
# $ # . #
#   #   #
## ## ## #
#  $  . #
#  @    #
#########
```

### 10. Nehéz
```
##########
#        #
# $$  .. #
#  #  #  #
# $$  .. #
#    @   #
##########
```

---

## Heurisztikák

### 1. Nulla heurisztika (BFS-t csinal)
```java
public int heur(SokobanState state) {
    return 0;
}
```

### 2. Dobozok szama amik nincsenek celon
```java
public int heur(SokobanState state) {
    int count = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                count++;
            }
        }
    }
    return count;
}
```

### 3. Manhattan-tavolsag (mohó)
```java
public int heur(SokobanState state) {
    int total = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                int minDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            int dist = Math.abs(r - gr) + Math.abs(c - gc);
                            minDist = Math.min(minDist, dist);
                        }
                    }
                }
                total += minDist;
            }
        }
    }
    return total;
}
```

### 4. Manhattan + jatekos tavolsaga a legkozelebbi doboztol
```java
public int heur(SokobanState state) {
    int total = 0;
    int minPlayerDist = Integer.MAX_VALUE;
    int pr = state.getPlayerRow();
    int pc = state.getPlayerCol();
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                int playerDist = Math.abs(pr - r) + Math.abs(pc - c);
                minPlayerDist = Math.min(minPlayerDist, playerDist);
                int minGoalDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            minGoalDist = Math.min(minGoalDist, Math.abs(r - gr) + Math.abs(c - gc));
                        }
                    }
                }
                total += minGoalDist;
            }
        }
    }
    if (minPlayerDist == Integer.MAX_VALUE) minPlayerDist = 0;
    return total + minPlayerDist;
}
```

### 5. Sulyozott: celon levo dobozok jutalma
```java
public int heur(SokobanState state) {
    int score = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                int minDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            minDist = Math.min(minDist, Math.abs(r - gr) + Math.abs(c - gc));
                        }
                    }
                }
                score += minDist * 2;
            }
        }
    }
    return score;
}
```

### 6. Sarokdetektalo (deadlock kerul)
```java
public int heur(SokobanState state) {
    int total = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                boolean wallUp = state.isWall(r - 1, c);
                boolean wallDown = state.isWall(r + 1, c);
                boolean wallLeft = state.isWall(r, c - 1);
                boolean wallRight = state.isWall(r, c + 1);
                if ((wallUp || wallDown) && (wallLeft || wallRight)) {
                    return 999999;
                }
                int minDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            minDist = Math.min(minDist, Math.abs(r - gr) + Math.abs(c - gc));
                        }
                    }
                }
                total += minDist;
            }
        }
    }
    return total;
}
```

### 7. Negyzetes tavolsag
```java
public int heur(SokobanState state) {
    int total = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                int minDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            int dr = r - gr;
                            int dc = c - gc;
                            minDist = Math.min(minDist, dr * dr + dc * dc);
                        }
                    }
                }
                total += minDist;
            }
        }
    }
    return total;
}
```

### 8. Max tavolsag (legrosszabb dobozra fokuszal)
```java
public int heur(SokobanState state) {
    int maxDist = 0;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                int minDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            minDist = Math.min(minDist, Math.abs(r - gr) + Math.abs(c - gc));
                        }
                    }
                }
                maxDist = Math.max(maxDist, minDist);
            }
        }
    }
    return maxDist;
}
```

### 9. Manhattan + sarokdetektalo + jatekos
```java
public int heur(SokobanState state) {
    int total = 0;
    int pr = state.getPlayerRow();
    int pc = state.getPlayerCol();
    int minPlayerDist = Integer.MAX_VALUE;
    for (int r = 0; r < state.getRows(); r++) {
        for (int c = 0; c < state.getCols(); c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                boolean wallUp = state.isWall(r - 1, c);
                boolean wallDown = state.isWall(r + 1, c);
                boolean wallLeft = state.isWall(r, c - 1);
                boolean wallRight = state.isWall(r, c + 1);
                if ((wallUp || wallDown) && (wallLeft || wallRight)) {
                    return 999999;
                }
                int minGoalDist = Integer.MAX_VALUE;
                for (int gr = 0; gr < state.getRows(); gr++) {
                    for (int gc = 0; gc < state.getCols(); gc++) {
                        if (state.isGoal(gr, gc)) {
                            minGoalDist = Math.min(minGoalDist, Math.abs(r - gr) + Math.abs(c - gc));
                        }
                    }
                }
                total += minGoalDist;
                minPlayerDist = Math.min(minPlayerDist, Math.abs(pr - r) + Math.abs(pc - c));
            }
        }
    }
    if (minPlayerDist == Integer.MAX_VALUE) minPlayerDist = 0;
    return total + minPlayerDist;
}
```

### 10. Konstans (nagyon rossz, de lefordul)
```java
public int heur(SokobanState state) {
    return 42;
}
```
