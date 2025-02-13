package net.coosanta.totalityloader.minecraft;

import com.mojang.logging.LogUtils;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

public class MinecraftClasses {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String mappingsResourcesPath = "/yarn-1.21.4-rc3_build4.tiny";
    private static final MappingTree mappings;

    public static final String SRC_NAMESPACE = "official";
    public static final String DST_NAMESPACE = "named";

    static {
        try {
            mappings = initiate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MinecraftClasses() {}

    public static MappingTree initiate() throws IOException {
        LOGGER.info("Loading mappings...");

        MemoryMappingTree mappingTree = new MemoryMappingTree();
        Path mappingPath = MinecraftClasses.extractResourceToTempFile(mappingsResourcesPath);
        MappingReader.read(mappingPath, mappingTree);

        mappingTree.setSrcNamespace("named");
        mappingTree.setDstNamespaces(Collections.singletonList("official"));

        LOGGER.info("Finished loading mappings.");

        return mappingTree;
    }

    public static MappingTree getMappings() {
        return mappings;
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
}
