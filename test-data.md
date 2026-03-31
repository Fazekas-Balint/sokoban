# Teszt adatok
//TELJESEN AI GENERÁLT PÁLYÁK ÉS KÓD (CLAUDE OPUS 4.6)

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

### 11. Szimmetrikus (6 doboz — a rendszer határa)
```
###########
#  .   .  #
#  #$$$#  #
#  . @ .  #
#  #$$$#  #
#  .   .  #
###########
```
6 doboz, 6 cél. A dobozok a középső folyosón vannak, a célok a sarkok felé.
A megoldáshoz a dobozokat ki kell tolni a folyosóról a falak közötti réseken,
majd oldalra navigálni a célokra. A sorrend kritikus: a szélsők mennek
először, a középsőket utoljára kell a maradék célokra tolni.
Jó heurisztikával ~5 000–30 000 csomópont alatt megoldható.

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

### 11. Moho parositas + harom deadlock-detektalas + jatekos (a legjobb ami az API-bol kihozható)
```java
public int heur(SokobanState state) {
    int rows = state.getRows();
    int cols = state.getCols();
    int[] bR = new int[50]; int[] bC = new int[50]; int nb = 0;
    int[] gR = new int[50]; int[] gC = new int[50]; int ng = 0;

    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            if (state.isBox(r, c) && !state.isGoal(r, c)) {
                boolean wU = state.isWall(r - 1, c);
                boolean wD = state.isWall(r + 1, c);
                boolean wL = state.isWall(r, c - 1);
                boolean wR = state.isWall(r, c + 1);

                if ((wU || wD) && (wL || wR)) return 999999;

                if (wU) {
                    boolean seg = true; boolean gSeg = false;
                    for (int cc = c; cc >= 0 && !state.isWall(r, cc); cc--) {
                        if (!state.isWall(r - 1, cc)) seg = false;
                        if (state.isGoal(r, cc)) gSeg = true;
                    }
                    for (int cc = c + 1; cc < cols && !state.isWall(r, cc); cc++) {
                        if (!state.isWall(r - 1, cc)) seg = false;
                        if (state.isGoal(r, cc)) gSeg = true;
                    }
                    if (seg && !gSeg) return 999999;
                }
                if (wD) {
                    boolean seg = true; boolean gSeg = false;
                    for (int cc = c; cc >= 0 && !state.isWall(r, cc); cc--) {
                        if (!state.isWall(r + 1, cc)) seg = false;
                        if (state.isGoal(r, cc)) gSeg = true;
                    }
                    for (int cc = c + 1; cc < cols && !state.isWall(r, cc); cc++) {
                        if (!state.isWall(r + 1, cc)) seg = false;
                        if (state.isGoal(r, cc)) gSeg = true;
                    }
                    if (seg && !gSeg) return 999999;
                }
                if (wL) {
                    boolean seg = true; boolean gSeg = false;
                    for (int rr = r; rr >= 0 && !state.isWall(rr, c); rr--) {
                        if (!state.isWall(rr, c - 1)) seg = false;
                        if (state.isGoal(rr, c)) gSeg = true;
                    }
                    for (int rr = r + 1; rr < rows && !state.isWall(rr, c); rr++) {
                        if (!state.isWall(rr, c - 1)) seg = false;
                        if (state.isGoal(rr, c)) gSeg = true;
                    }
                    if (seg && !gSeg) return 999999;
                }
                if (wR) {
                    boolean seg = true; boolean gSeg = false;
                    for (int rr = r; rr >= 0 && !state.isWall(rr, c); rr--) {
                        if (!state.isWall(rr, c + 1)) seg = false;
                        if (state.isGoal(rr, c)) gSeg = true;
                    }
                    for (int rr = r + 1; rr < rows && !state.isWall(rr, c); rr++) {
                        if (!state.isWall(rr, c + 1)) seg = false;
                        if (state.isGoal(rr, c)) gSeg = true;
                    }
                    if (seg && !gSeg) return 999999;
                }

                for (int dr = -1; dr <= 0; dr++) {
                    for (int dc = -1; dc <= 0; dc++) {
                        int tr = r + dr; int tc = c + dc;
                        boolean blocked = true;
                        int bc = 0; int gc = 0;
                        for (int rr = tr; rr <= tr + 1; rr++) {
                            for (int cc = tc; cc <= tc + 1; cc++) {
                                if (state.isBox(rr, cc)) bc++;
                                else if (!state.isWall(rr, cc)) blocked = false;
                                if (state.isGoal(rr, cc)) gc++;
                            }
                        }
                        if (blocked && gc < bc) return 999999;
                    }
                }

                bR[nb] = r; bC[nb] = c; nb++;
            }
            if (state.isGoal(r, c) && !state.isBox(r, c)) {
                gR[ng] = r; gC[ng] = c; ng++;
            }
        }
    }
    if (nb == 0) return 0;

    boolean[] ub = new boolean[nb]; boolean[] ug = new boolean[ng];
    int total = 0;
    for (int i = 0; i < nb; i++) {
        int best = 999999; int bi = -1; int gi = -1;
        for (int b = 0; b < nb; b++) {
            if (ub[b]) continue;
            for (int g = 0; g < ng; g++) {
                if (ug[g]) continue;
                int d = Math.abs(bR[b] - gR[g]) + Math.abs(bC[b] - gC[g]);
                if (d < best) { best = d; bi = b; gi = g; }
            }
        }
        if (bi >= 0) { ub[bi] = true; ug[gi] = true; total += best; }
    }

    int pr = state.getPlayerRow(); int pc = state.getPlayerCol();
    int mp = 999999;
    for (int b = 0; b < nb; b++) {
        int d = Math.abs(pr - bR[b]) + Math.abs(pc - bC[b]);
        if (d < mp) mp = d;
    }
    return total + mp;
}
```
