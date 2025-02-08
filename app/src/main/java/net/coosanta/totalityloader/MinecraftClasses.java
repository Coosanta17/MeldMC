package net.coosanta.totalityloader;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinecraftClasses {
    private static final Map<String, String> classMappings = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> methodMappings = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> fieldMappings = new ConcurrentHashMap<>();

    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void initiate() throws IOException {
        MemoryMappingTree mappingTree = new MemoryMappingTree();
        Path mappingPath = MinecraftClasses.extractResourceToTempFile("/yarn-1.21.4-rc3_build4.tiny");
        MappingReader.read(mappingPath, mappingTree);

        executorService.submit(() -> loadMappings(mappingTree));
    }

    private static void loadMappings(MemoryMappingTree mappingTree) {
        // Class mappings
        mappingTree.getClasses().forEach(classDef -> {
            String obfuscatedClassName = classDef.getName("official");
            String deobfuscatedClassName = classDef.getName("named");
            if (obfuscatedClassName != null && deobfuscatedClassName != null) {
                classMappings.put(deobfuscatedClassName, obfuscatedClassName);

                // Method mappings
                executorService.submit(() -> loadMethodMappings(classDef, deobfuscatedClassName));

                // Variable mappings
                executorService.submit(() -> loadFieldMappings(classDef, deobfuscatedClassName));
            }
        });
    }

    private static void loadFieldMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
        Map<String, String> classFieldMappings = new HashMap<>();
        classDef.getFields().forEach(fieldDef -> {
            String obfuscatedFieldName = fieldDef.getName("official");
            String deobfuscatedFieldName = fieldDef.getName("named");
            if (obfuscatedFieldName != null && deobfuscatedFieldName != null) {
                classFieldMappings.put(deobfuscatedFieldName, obfuscatedFieldName);
            }
        });
        fieldMappings.put(deobfuscatedClassName, classFieldMappings);
    }

    private static void loadMethodMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
        Map<String, String> classMethodMappings = new HashMap<>();
        classDef.getMethods().forEach(methodDef -> {
            String obfuscatedMethodName = methodDef.getName("official");
            String deobfuscatedMethodName = methodDef.getName("named");
            if (obfuscatedMethodName != null && deobfuscatedMethodName != null) {
                classMethodMappings.put(deobfuscatedMethodName, obfuscatedMethodName);
            }
        });
        methodMappings.put(deobfuscatedClassName, classMethodMappings);
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
}
