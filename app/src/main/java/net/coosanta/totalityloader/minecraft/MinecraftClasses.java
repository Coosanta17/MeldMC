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
    private static MappingTree mappings;

    public static final String SRC_NAMESPACE = "official";
    public static final String DST_NAMESPACE = "named";

    public MinecraftClasses() {}

    public static void initiate() throws IOException {
        LOGGER.info("Loading mappings...");

        MemoryMappingTree mappingTree = new MemoryMappingTree();
        Path mappingPath = MinecraftClasses.extractResourceToTempFile(mappingsResourcesPath);
        MappingReader.read(mappingPath, mappingTree);

        mappingTree.setSrcNamespace(SRC_NAMESPACE);
        mappingTree.setDstNamespaces(Collections.singletonList(DST_NAMESPACE));

        mappings = mappingTree;

        LOGGER.info("Finished loading mappings.");
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
