package net.coosanta.totalityloader;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MinecraftClasses implements Serializable {
    private static boolean isComplete = false;
    private static byte version = 1;
    private static final Map<String, String> classMappings = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> methodMappings = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> fieldMappings = new ConcurrentHashMap<>();

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());;

    private static long totalMappings;
    private static AtomicLong processedMappings = new AtomicLong(0);

    public static void initiate() throws IOException {
        if (version == 1 && isComplete) return;

        MemoryMappingTree mappingTree = new MemoryMappingTree();
        Path mappingPath = MinecraftClasses.extractResourceToTempFile("/yarn-1.21.4-rc3_build4.tiny");
        MappingReader.read(mappingPath, mappingTree);

        totalMappings = mappingTree.getClasses().size();

        try {
            loadMappings(mappingTree);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void loadMappings(MemoryMappingTree mappingTree) throws InterruptedException {
        // Class mappings
        mappingTree.getClasses().forEach(classDef -> executorService.submit(() -> loadClassMappings(classDef)));
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            isComplete = false;
            throw new RuntimeException("Mapping tasks did not finish in 10 minutes! Either your computer is too slow, or something got stuck.");
        }
        if (processedMappings.get() == totalMappings) {
            isComplete = true;
        } else {
            throw new RuntimeException("Did not complete mappings. Completed " + processedMappings + "out of" + totalMappings + " classes.");
        }
    }

    private static void loadClassMappings(MappingTree.ClassMapping classDef) {
        try {
            String obfuscatedClassName = classDef.getName("official");
            String deobfuscatedClassName = classDef.getName("named");
            if (obfuscatedClassName != null && deobfuscatedClassName != null) {
                classMappings.put(deobfuscatedClassName, obfuscatedClassName);

                // Method mappings
                loadMethodMappings(classDef, deobfuscatedClassName);

                // Variable mappings
                loadFieldMappings(classDef, deobfuscatedClassName);

                processedMappings.incrementAndGet();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing class mappings.", e);
        }
    }

    private static void loadFieldMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
        try {
            Map<String, String> classFieldMappings = new ConcurrentHashMap<>();
            classDef.getFields().forEach(fieldDef -> {
                String obfuscatedFieldName = fieldDef.getName("official");
                String deobfuscatedFieldName = fieldDef.getName("named");
                if (obfuscatedFieldName != null && deobfuscatedFieldName != null) {
                    classFieldMappings.put(deobfuscatedFieldName, obfuscatedFieldName);
                }
            });
            fieldMappings.put(deobfuscatedClassName, classFieldMappings);
        } catch (Exception e) {
            System.err.printf("Error loading fields for class \"%1$s\", %n%2$s", deobfuscatedClassName, e.getMessage());
        }
    }

    private static void loadMethodMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
        try {
            Map<String, String> classMethodMappings = new ConcurrentHashMap<>();
            classDef.getMethods().forEach(methodDef -> {
                String obfuscatedMethodName = methodDef.getName("official");
                String deobfuscatedMethodName = methodDef.getName("named");
                if (obfuscatedMethodName != null && deobfuscatedMethodName != null) {
                    classMethodMappings.put(deobfuscatedMethodName, obfuscatedMethodName);
                }
            });
            methodMappings.put(deobfuscatedClassName, classMethodMappings);
        } catch (Exception e) {
            System.err.printf("Error loading methods for class \"%1s\", %n%2s", deobfuscatedClassName, e.getMessage());
        }
    }

    public static String getObfuscatedClassName(String deobfuscatedName) {
        return classMappings.getOrDefault(deobfuscatedName, deobfuscatedName);
    }

    public static Class<?> getObfuscatedClass(String deobfuscatedName) throws ClassNotFoundException {
        String obfuscatedName = getObfuscatedClassName(deobfuscatedName);
        return Class.forName(obfuscatedName);
    }

    public static String getObfuscatedMethodName(String className, String deobfuscatedMethodName) {
        Map<String, String> classMethodMappings = methodMappings.get(className);
        if (classMethodMappings != null) {
            return classMethodMappings.getOrDefault(deobfuscatedMethodName, deobfuscatedMethodName);
        }
        return deobfuscatedMethodName;
    }

    public static String getObfuscatedFieldName(String className, String deobfuscatedFieldName) {
        Map<String, String> classFieldMappings = fieldMappings.get(className);
        if (classFieldMappings != null) {
            return classFieldMappings.getOrDefault(deobfuscatedFieldName, deobfuscatedFieldName);
        }
        return deobfuscatedFieldName;
    }

    public static Method getObfusicatedMethod(Class<?> clazz, String deobfuscatedMethodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        String obfuscatedMethodName = getObfuscatedMethodName(clazz.getName(), deobfuscatedMethodName);
        return clazz.getDeclaredMethod(obfuscatedMethodName, parameterTypes);
    }

    private static Path extractResourceToTempFile(String resourcePath) throws IOException {
        InputStream inputStream = MinecraftClasses.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        Path tempFile = Files.createTempFile("mappings", ".tiny");
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();

        return tempFile;
    }

    public static byte getVersion() {
        return version;
    }
}
