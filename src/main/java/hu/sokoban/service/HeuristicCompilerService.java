package hu.sokoban.service;

import hu.sokoban.engine.HeuristicFunction;

public interface HeuristicCompilerService {

    CompilationResult compile(Long heuristicId, String sourceCode);

    HeuristicFunction loadCompiled(Long heuristicId);

    record CompilationResult(boolean success, String errorMessage) {

        public static CompilationResult ok() {
            return new CompilationResult(true, null);
        }

        public static CompilationResult error(String message) {
            return new CompilationResult(false, message);
        }
    }
}
