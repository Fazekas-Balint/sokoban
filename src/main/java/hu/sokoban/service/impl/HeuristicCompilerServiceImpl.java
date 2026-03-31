package hu.sokoban.service.impl;

import hu.sokoban.engine.HeuristicFunction;
import hu.sokoban.service.HeuristicCompilerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HeuristicCompilerServiceImpl implements HeuristicCompilerService {

    private static final Logger log = LoggerFactory.getLogger(HeuristicCompilerServiceImpl.class);

    private static final String CLASS_PREFIX = "UserHeuristic_";
    private static final String PACKAGE_NAME = "hu.sokoban.generated";

    /**
     * Tiltott kulcsszavak/osztalyok a felhasznaloi kodban.
     * Ezek megakadalyozzak a fajlrendszer-, halozat-, process- es reflection-hozzaferest.
     */
    private static final List<String> FORBIDDEN_PATTERNS = List.of(
            // Fajlrendszer
            "java.io.File", "java.nio", "FileInputStream", "FileOutputStream",
            "FileReader", "FileWriter", "RandomAccessFile", "Files.",
            "Paths.", "Path.", "File.",
            // Halozat
            "java.net", "Socket", "ServerSocket", "URL", "URI",
            "HttpClient", "HttpURLConnection", "DatagramSocket",
            // Process / Runtime
            "Runtime", "ProcessBuilder", "Process",
            // Reflection
            "java.lang.reflect", "Class.forName", "getClass()",
            "getDeclaredMethod", "getDeclaredField", "setAccessible",
            "Method.", "Field.", "Constructor.",
            // ClassLoader / bytecode
            "ClassLoader", "defineClass", "URLClassLoader",
            // System
            "System.exit", "System.setProperty", "System.getenv",
            "System.setOut", "System.setErr", "System.setIn",
            // Thread
            "Thread", "Runnable", "ExecutorService", "Executor",
            "CompletableFuture", "ForkJoinPool",
            // Unsafe / JNI
            "Unsafe", "native ",
            // Import (csak engine importot engedunk)
            "import "
    );

    private final Map<Long, Class<?>> compiledClasses = new ConcurrentHashMap<>();

    @Override
    public CompilationResult compile(Long heuristicId, String sourceCode) {
        // Biztonsagi ellenorzes: tiltott mintak keresese
        String securityError = checkForbiddenPatterns(sourceCode);
        if (securityError != null) {
            return CompilationResult.error(securityError);
        }

        String className = CLASS_PREFIX + heuristicId;
        String fullClassName = PACKAGE_NAME + "." + className;

        String fullSource = generateWrapperSource(className, sourceCode);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return CompilationResult.error("Java compiler nem elerheto (JDK szukseges, nem JRE)");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryFileManager fileManager = new InMemoryFileManager(
                compiler.getStandardFileManager(diagnostics, null, null));

        JavaFileObject sourceFile = new InMemorySourceFile(fullClassName, fullSource);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, null, null, List.of(sourceFile));

        boolean success = task.call();

        if (!success) {
            StringBuilder errors = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
                if (diag.getKind() == Diagnostic.Kind.ERROR) {
                    errors.append("Sor ").append(diag.getLineNumber())
                          .append(": ").append(diag.getMessage(Locale.forLanguageTag("hu")))
                          .append("\n");
                }
            }
            return CompilationResult.error(errors.toString().trim());
        }

        try {
            InMemoryClassLoader classLoader = new InMemoryClassLoader(fileManager.getCompiledClasses());
            Class<?> clazz = classLoader.loadClass(fullClassName);
            compiledClasses.put(heuristicId, clazz);
            return CompilationResult.ok();
        } catch (ClassNotFoundException e) {
            log.error("Leforditott osztaly nem talalhato: {}", fullClassName, e);
            return CompilationResult.error("Belso hiba: az osztaly nem toltheto be");
        }
    }

    @Override
    public HeuristicFunction loadCompiled(Long heuristicId) {
        Class<?> clazz = compiledClasses.get(heuristicId);
        if (clazz == null) {
            throw new IllegalStateException("A heurisztika nincs leforditva: " + heuristicId);
        }
        try {
            return (HeuristicFunction) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Nem sikerult peldanyositani a heuristzikat: " + heuristicId, e);
        }
    }

    /**
     * Ellenorzi, hogy a felhasznaloi kod tartalmaz-e tiltott mintakat.
     * @return hibauzenet, vagy null ha rendben van
     */
    private String checkForbiddenPatterns(String sourceCode) {
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (sourceCode.contains(pattern)) {
                log.warn("Tiltott minta talalva a heurisztika kodban: {}", pattern);
                return "Biztonsagi hiba: tiltott kod minta: \"" + pattern + "\". "
                        + "A heurisztika csak a SokobanState API-t hasznalhatja, "
                        + "kulon importok es rendszerhivasok nem engedelyezettek.";
            }
        }
        return null;
    }

    private String generateWrapperSource(String className, String sourceCode) {
        return """
                package %s;

                import hu.sokoban.engine.HeuristicFunction;
                import hu.sokoban.engine.SokobanState;

                public class %s implements HeuristicFunction {
                    @Override
                    %s
                }
                """.formatted(PACKAGE_NAME, className, sourceCode);
    }

    // --- In-memory forditas segitosztalyai ---

    private static class InMemorySourceFile extends SimpleJavaFileObject {
        private final String code;

        InMemorySourceFile(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                  Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static class InMemoryClassFile extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InMemoryClassFile(String name) {
            super(URI.create("mem:///" + name.replace('.', '/') + Kind.CLASS.extension),
                  Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }

        byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    private static class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, InMemoryClassFile> compiledClasses = new HashMap<>();

        InMemoryFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                    JavaFileObject.Kind kind, FileObject sibling) {
            InMemoryClassFile classFile = new InMemoryClassFile(className);
            compiledClasses.put(className, classFile);
            return classFile;
        }

        Map<String, byte[]> getCompiledClasses() {
            Map<String, byte[]> result = new HashMap<>();
            compiledClasses.forEach((name, file) -> result.put(name, file.getBytes()));
            return result;
        }
    }

    private static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;

        InMemoryClassLoader(Map<String, byte[]> classBytes) {
            super(InMemoryClassLoader.class.getClassLoader());
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
