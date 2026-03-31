package hu.sokoban.engine;

/**
 * Strategy pattern: a felhasznalok altal megirt heurisztikak ezt az interfeszt implementaljak.
 */
@FunctionalInterface
public interface HeuristicFunction {

    int heur(SokobanState state);
}
