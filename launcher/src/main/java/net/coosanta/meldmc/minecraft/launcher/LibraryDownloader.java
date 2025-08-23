package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Handles downloading and managing Minecraft libraries
 */
public class LibraryDownloader {
    private static final Logger log = LoggerFactory.getLogger(LibraryDownloader.class);

    private final Path librariesDir;
    private final Path nativesDir;
    private final ExecutorService downloadExecutor;
    private final RuleEvaluator ruleEvaluator;

    public LibraryDownloader(Path librariesDir, Path nativesDir, ExecutorService downloadExecutor, RuleEvaluator ruleEvaluator) {
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.downloadExecutor = downloadExecutor;
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Download all required libraries and return classpath
     */
    public List<Path> downloadLibraries(ObjectNode clientData) {
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        List<Path> classpath = new ArrayList<>();

        if (!clientData.has("libraries")) return classpath;

        ArrayNode libraries = (ArrayNode) clientData.get("libraries");
        for (JsonNode libElement : libraries) {
            ObjectNode lib = (ObjectNode) libElement;

            if (!ruleEvaluator.passesOsRule(lib)) continue;

            boolean isNative = isNativeLibrary(lib);
            ObjectNode artifactNode = resolveArtifactNode(lib, isNative);

            if (artifactNode == null) continue;

            Path libPath = getLibraryPath(artifactNode, lib);
            if (!isNative) classpath.add(libPath);

            if (Files.exists(libPath) && isValidFile(artifactNode, libPath)) {
                if (isNative) tasks.add(extractAsync(libPath));
                continue;
            }

            tasks.add(downloadAndMaybeExtract(lib, artifactNode, libPath, isNative));
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        return classpath;
    }

    private boolean isNativeLibrary(ObjectNode lib) {
        String name = lib.path("name").asText("");
        return name.contains(":natives-") || lib.path("downloads").has("classifiers") || lib.has("natives");
    }

    private ObjectNode resolveArtifactNode(ObjectNode lib, boolean isNative) {
        ObjectNode downloads = (ObjectNode) lib.path("downloads");
        if (!isNative) {
            JsonNode artifact = downloads.path("artifact");
            return artifact.isObject() ? (ObjectNode) artifact : null;
        }

        ObjectNode classifiers = downloads.has("classifiers") && downloads.get("classifiers").isObject()
                ? (ObjectNode) downloads.get("classifiers") : null;
        if (classifiers != null) {
            for (String key : nativeClassifierCandidates()) {
                if (classifiers.has(key) && classifiers.get(key).isObject()) {
                    return (ObjectNode) classifiers.get(key);
                }
            }
        }

        JsonNode artifact = downloads.path("artifact");
        if (artifact.isObject()) return (ObjectNode) artifact;

        return null;
    }

    private List<String> nativeClassifierCandidates() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        NativeExtractor.OS os = NativeExtractor.currentOS();
        List<String> keys = new ArrayList<>();

        switch (os) {
            case WINDOWS -> {
                if (arch.contains("aarch64") || arch.contains("arm64")) keys.add("natives-windows-arm64");
                else if (arch.contains("86") && !arch.contains("64")) keys.add("natives-windows-x86");
                else keys.add("natives-windows");
            }
            case MAC -> {
                if (arch.contains("aarch64") || arch.contains("arm64")) keys.add("natives-macos-arm64");
                else {
                    keys.add("natives-macos");
                    keys.add("natives-osx"); // Legacy
                }
            }
            case LINUX -> keys.add("natives-linux"); // No arm64 support yet
        }
        return keys;
    }

    private CompletableFuture<Void> extractAsync(Path libPath) {
        return CompletableFuture.runAsync(() -> extractNative(libPath), downloadExecutor);
    }

    private void extractNative(Path libPath) {
        try {
            NativeExtractor.extractNatives(libPath, nativesDir);
            log.debug("Extracted natives from {}", libPath.getFileName());
        } catch (Exception e) {
            throw new RuntimeException("Failed extracting natives: " + libPath, e);
        }
    }

    private CompletableFuture<Void> downloadAndMaybeExtract(ObjectNode lib, ObjectNode artifactNode, Path libPath, boolean isNative) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = getLibraryUrl(artifactNode, lib);
                Files.createDirectories(libPath.getParent());
                FileDownloader.downloadFile(url, libPath);
                log.debug("Downloaded library: {}", lib.path("name").asText());

                if (isNative) extractNative(libPath);

            } catch (Exception e) {
                log.error("Failed to download library: {}", lib.path("name").asText(), e);
                throw new RuntimeException(e);
            }
        }, downloadExecutor);
    }

    private Path getLibraryPath(ObjectNode artifactNode, ObjectNode lib) {
        if (artifactNode.has("path")) {
            return librariesDir.resolve(artifactNode.get("path").asText());
        }
        if (artifactNode.has("url")) {
            String url = artifactNode.get("url").asText();
            String repoPath = URI.create(url).getPath();
            if (repoPath.startsWith("/")) repoPath = repoPath.substring(1);
            return librariesDir.resolve(repoPath);
        }
        return librariesDir.resolve(buildUrlFromName(lib));
    }

    private String getLibraryUrl(ObjectNode artifactNode, ObjectNode lib) {
        if (artifactNode.has("url")) return artifactNode.get("url").asText();

        if (lib.has("url")) {
            String base = lib.get("url").asText();
            if (!base.endsWith("/")) base += "/";
            return base + buildUrlFromName(lib);
        }
        return "https://libraries.minecraft.net/" + buildUrlFromName(lib);
    }

    private String buildUrlFromName(ObjectNode lib) {
        String[] parts = lib.get("name").asText().split(":");
        String group = parts[0], artifact = parts[1], version = parts[2];
        String classifier = parts.length > 3 ? "-" + parts[3] : "";
        return group.replace(".", "/") + "/" + artifact + "/" + version + "/"
               + artifact + "-" + version + classifier + ".jar";
    }

    private boolean isValidFile(ObjectNode artifactNode, Path file) {
        try {
            String expectedSha1 = artifactNode.has("sha1") ? artifactNode.get("sha1").asText(null) : null;
            Long expectedSize = artifactNode.has("size") ? artifactNode.get("size").asLong() : null;

            if (expectedSha1 != null && expectedSize != null) {
                return expectedSha1.equals(ResourceUtil.calculateSHA1(file.toFile())) && expectedSize == Files.size(file);
            }
            if (expectedSha1 != null) {
                return expectedSha1.equals(ResourceUtil.calculateSHA1(file.toFile()));
            }
            if (expectedSize != null) {
                return expectedSize == Files.size(file);
            }
            return Files.exists(file);
        } catch (Exception e) {
            return false;
        }
    }
}
