package net.coosanta.meldmc.minecraft.launcher;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coosanta.meldmc.minecraft.GameInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientLauncher {
    private static final Logger log = LoggerFactory.getLogger(ClientLauncher.class);

    static final Path minecraftDir;
    static final Path librariesDir;
    static final Path versionsDir;
    static final Path assetsDir;
    static final Path nativesDir;

    // Components
    private final ClientJsonResolver jsonResolver;
    private final LibraryDownloader libraryDownloader;
    private final CommandBuilder commandBuilder;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            minecraftDir = Path.of(System.getenv("APPDATA"), ".minecraft");
        } else if (osName.contains("mac")) {
            minecraftDir = Path.of(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
        } else {
            minecraftDir = Path.of(System.getProperty("user.home"), ".minecraft");
        }

        librariesDir = minecraftDir.resolve("libraries");
        versionsDir = minecraftDir.resolve("versions");
        assetsDir = minecraftDir.resolve("assets");
        // TODO: Minecraft launcher uses some sort of SHA1 concatenation to get the natives directory
        try {
            nativesDir = Files.createTempDirectory("minecraft-natives"); // TODO: Cleanup
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (var stream = Files.walk(nativesDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to cleanup temporary file '{}'", path, e);
                            }
                        });
                Files.deleteIfExists(nativesDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public ClientLauncher() {
        createDirectories();

        ExecutorService downloadExecutor = Executors.newFixedThreadPool(
                3 * Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r, "library-downloader");
                    t.setDaemon(true);
                    return t;
                });

        this.jsonResolver = new ClientJsonResolver(versionsDir);
        var ruleEvaluator = new RuleEvaluator();
        this.libraryDownloader = new LibraryDownloader(librariesDir, nativesDir, downloadExecutor, ruleEvaluator);
        this.commandBuilder = new CommandBuilder(ruleEvaluator);
    }

    private void createDirectories() {
        try {
            Files.createDirectories(librariesDir);
            Files.createDirectories(versionsDir);
            Files.createDirectories(assetsDir);
            Files.createDirectories(nativesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create launcher directories", e);
        }
    }

    /**
     * Launch Minecraft using LaunchArgs configuration
     */
    public Process launch(GameInstance instance, LaunchArgs launchArgs) throws Exception {
        if (instance.getMeldData() == null) throw new IllegalStateException("Instance is missing Meld data");

        log.info("Launching Minecraft for instance: {}", instance.getAddress());

        ObjectNode clientData = jsonResolver.loadClientJson(instance.getMeldData().versionId());

        List<Path> classpath = libraryDownloader.downloadLibraries(clientData);

        addClientJarToClasspath(clientData, classpath);

        List<String> command = commandBuilder.buildCommand(clientData, classpath, launchArgs);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(instance.getInstanceDir().toFile());
        pb.inheritIO();

        Process process = pb.start();
        log.info("Started Minecraft process (PID: {})", process.pid());

        return process;
    }

    private void addClientJarToClasspath(ObjectNode clientData, List<Path> classpath) {
//        String versionId = clientData.get("id").asText();
//        Path clientJar = versionsDir.resolve(versionId).resolve(versionId + ".jar");
//
//        if (Files.exists(clientJar)) {
//            classpath.add(clientJar);
//        } else {
//            log.warn("Client jar not found: {}", clientJar);
//            downloadClientJarIfAvailable(clientData, clientJar);
//        }
    }

    private void downloadClientJarIfAvailable(ObjectNode clientData, Path jarPath) {
        try {
            var dlNode = clientData.path("downloads").path("client");
            String url = dlNode.path("url").asText(null);
            if (url == null || url.isBlank()) {
                log.warn("No client download URL; skipping client.jar download");
                return;
            }

            FileDownloader.downloadFile(url, jarPath);
        } catch (Exception e) {
            log.error("Client jar download failed.", e);
        }
    }

    // Debug
    public Process launchStandalone(String versionId, Path instanceDir, LaunchArgs launchArgs) throws Exception {
        if (versionId == null || versionId.isBlank()) {
            throw new IllegalArgumentException("versionId is required");
        }
        if (instanceDir == null) {
            throw new IllegalArgumentException("instanceDir is required");
        }
        log.info("Launching Minecraft standalone. versionId={}, instanceDir={}", versionId, instanceDir);

        ObjectNode clientData = jsonResolver.loadClientJson(versionId);

        List<Path> classpath = libraryDownloader.downloadLibraries(clientData);
        addClientJarToClasspath(clientData, classpath);

        List<String> command = commandBuilder.buildCommand(clientData, classpath, launchArgs);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(instanceDir.toFile());
        pb.inheritIO();

        Process process = pb.start();
        log.info("Started Minecraft process (PID: {})", process.pid());
        return process;
    }

    public static void main(String[] args) throws Exception {
        // --version <id> --instanceDir <path> --launchArgs <args>
        String versionId = null;
        Path instanceDir = null;
        LaunchArgs launchArgs = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--version":
                    if (i + 1 < args.length) versionId = args[++i];
                    break;
                case "--instanceDir":
                    if (i + 1 < args.length) instanceDir = Path.of(args[++i]);
                    break;
                case "--launchArgs":
                    if (i + 1 < args.length) {
                        // take all remaining args
                        String[] rest = Arrays.copyOfRange(args, ++i, args.length);
                        launchArgs = LaunchArgs.parse(rest);
                        // jump to end so we don’t re-scan those
                        i = args.length;
                    }
                    break;
                default:
                    System.out.println("Unknown arg: " + args[i]);
            }
        }

        ClientLauncher launcher = new ClientLauncher();
        Process p = launcher.launchStandalone(versionId, instanceDir, launchArgs);
        int exit = p.waitFor();
        System.out.println("Minecraft exited with code: " + exit);
    }
}
