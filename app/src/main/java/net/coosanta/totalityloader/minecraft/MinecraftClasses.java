package net.coosanta.totalityloader.minecraft;

import com.mojang.logging.LogUtils;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
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
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final MinecraftClasses instance;

    private static final String FILE = "yarnMappings-1.21.4.ser";

    static {
        try {
            instance = new MinecraftClasses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isComplete = false;
    private byte version = 1;

    private final Map<String, String> classMappings = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> methodMappings = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> fieldMappings = new ConcurrentHashMap<>();

    private static ExecutorService executorService = null;

    private static long totalMappings;
    private static final AtomicLong processedMappings = new AtomicLong(0);

    private MinecraftClasses() throws IOException {
        LOGGER.info("loading Minecraft class mappings");
        try (FileInputStream fileIn = new FileInputStream(FILE);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {

            MinecraftClasses tempInstance = (MinecraftClasses) in.readObject();
            this.classMappings.putAll(tempInstance.classMappings);
            this.methodMappings.putAll(tempInstance.methodMappings);
            this.fieldMappings.putAll(tempInstance.fieldMappings);
            this.isComplete = tempInstance.isComplete;
            this.version = tempInstance.version;

            LOGGER.debug("Loaded mappings from file.");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.debug("Cannot find mapping cache file, creating one.");
        }
        this.initiate();
    }

    private void initiate() throws IOException {
        if (version == 1 && isComplete) return;

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        if (!classMappings.isEmpty() || !methodMappings.isEmpty() || !fieldMappings.isEmpty()) {
            LOGGER.warn("Clearing incomplete mappings from unfinished session");
            classMappings.clear();
            methodMappings.clear();
            fieldMappings.clear();
        }

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

    private void loadMappings(MemoryMappingTree mappingTree) throws InterruptedException {
        // Class mappings
        mappingTree.getClasses().forEach(classDef -> executorService.submit(() -> loadClassMappings(classDef)));
        executorService.shutdown();

        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            isComplete = false;
            executorService.shutdownNow();
            throw new RuntimeException("Mapping tasks did not finish in 10 minutes! Either your computer is too slow, or something got stuck.");
        }
        if (processedMappings.get() == totalMappings) {
            isComplete = true;
            serialize();
        } else {
            throw new RuntimeException("Did not complete mappings. Completed " + processedMappings + "out of" + totalMappings + " classes.");
        }
    }

    private void serialize() {
        try (FileOutputStream fileOut = new FileOutputStream(FILE);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);
            LOGGER.debug("Serialized mapping cache");
        } catch (IOException e) {
            LOGGER.error("Cannot save mappings to file. It will still work but it will have to reload them every time at startup.", e);
        }
    }

    private void loadClassMappings(MappingTree.ClassMapping classDef) {
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

    private void loadFieldMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
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
            LOGGER.error("Error loading fields for class \"{}\".\n{}", deobfuscatedClassName, e);
        }
    }

    private void loadMethodMappings(MappingTree.ClassMapping classDef, String deobfuscatedClassName) {
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
            LOGGER.error("Error loading methods for class \"{}\".\n{}", deobfuscatedClassName, e);
        }
    }

    public static MinecraftClasses getInstance() {
        return instance;
    }

    private String getObfuscatedClassName(String deobfuscatedName) {
        return this.classMappings.getOrDefault(deobfuscatedName, null);
    }

    public Class<?> getObfuscatedClass(String deobfuscatedName) throws ClassNotFoundException {
        String obfuscatedName = getObfuscatedClassName(deobfuscatedName);
        LOGGER.debug("Obfuscated class name: {}", deobfuscatedName);
        return Class.forName(obfuscatedName);
    }

    private String getObfuscatedMethodName(String className, String deobfuscatedMethodName) {
        Map<String, String> classMethodMappings = methodMappings.get(className);
        if (classMethodMappings != null) {
            return classMethodMappings.getOrDefault(deobfuscatedMethodName, null);
        }
        return deobfuscatedMethodName;
    }

    public Method getObfuscatedMethod(Class<?> clazz, String deobfuscatedMethodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        String obfuscatedMethodName = getObfuscatedMethodName(clazz.getName(), deobfuscatedMethodName);
        return clazz.getDeclaredMethod(obfuscatedMethodName, parameterTypes);
    }

    private String getObfuscatedFieldName(String className, String deobfuscatedFieldName) {
        Map<String, String> classFieldMappings = fieldMappings.get(className);
        if (classFieldMappings != null) {
            return classFieldMappings.getOrDefault(deobfuscatedFieldName, null);
        }
        return deobfuscatedFieldName;
    }

    public Field getObfuscatedField(Class<?> clazz, String deobfuscatedFieldName) throws NoSuchFieldException {
        String obfuscatedFieldName = getObfuscatedFieldName(clazz.getName(), deobfuscatedFieldName);
        return clazz.getDeclaredField(obfuscatedFieldName);
    }

    private static Path extractResourceToTempFile(String resourcePath) throws IOException {
        InputStream inputStream = MinecraftClasses.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        Path tempFile = Files.createTempFile("mappings", ".tiny");
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit();
        inputStream.close();

        return tempFile;
    }

    public byte getVersion() {
        return version;
    }
}
