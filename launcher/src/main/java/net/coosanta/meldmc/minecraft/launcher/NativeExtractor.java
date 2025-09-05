package net.coosanta.meldmc.minecraft.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class NativeExtractor {
    private static final Logger log = LoggerFactory.getLogger(NativeExtractor.class);

    enum OS {WINDOWS, LINUX, MAC}

    private static final Map<Path, Object> LOCKS = new ConcurrentHashMap<>();

    private NativeExtractor() {
    }

    static OS currentOS() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        if (os.contains("mac") || os.contains("os x") || os.contains("darwin")) return OS.MAC;
        return OS.LINUX;
    }

    static void extractNatives(Path jarFile, Path targetDir) throws IOException {
        if (!jarMatchesCurrentArch(jarFile)) {
            log.debug("Skipping natives (classifier not matching arch): {}", jarFile);
            return;
        }

        Files.createDirectories(targetDir);
        try (InputStream in = Files.newInputStream(jarFile);
             ZipInputStream zin = new ZipInputStream(in)) {

            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                extractNative(targetDir, e, zin);
            }
        }
    }

    private static void extractNative(Path targetDir, ZipEntry e, ZipInputStream zin) throws IOException {
        String name = e.getName();
        if (e.isDirectory() || name.startsWith("META-INF/") || !isNativeFile(name)) return;

        Path out = targetDir.resolve(name.substring(name.lastIndexOf('/') + 1)).toAbsolutePath().normalize();
        Files.createDirectories(out.getParent() == null ? targetDir : out.getParent());

        Object lock = LOCKS.computeIfAbsent(out, p -> new Object());
        synchronized (lock) {
            if (Files.exists(out)) {
                return;
            }

            Path tmp = Files.createTempFile(targetDir, "native-", ".tmp");
            try {
                Files.copy(zin, tmp, StandardCopyOption.REPLACE_EXISTING);
                try {
                    Files.move(tmp, out, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    // Fallback to non-atomic
                    try {
                        Files.move(tmp, out);
                    } catch (FileAlreadyExistsException ignore) {
                        // Another thread won the race
                    }
                }
            } catch (AccessDeniedException | FileAlreadyExistsException ignore) {
                // Already locked
            } finally {
                Files.deleteIfExists(tmp);
            }

            // Ensure executable bit on Unix
            if (currentOS() != OS.WINDOWS) {
                try {
                    Set<PosixFilePermission> perms = EnumSet.of(
                            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE
                    );
                    Files.setPosixFilePermissions(out, perms);
                } catch (UnsupportedOperationException ignored) {
                    // Non-POSIX FS
                }
            }
        }
    }

    private static boolean isNativeFile(String name) {
        String lower = name.toLowerCase();
        return switch (currentOS()) {
            case WINDOWS -> lower.endsWith(".dll");
            case MAC -> lower.endsWith(".dylib");
            default -> lower.endsWith(".so");
        };
    }

    private static boolean jarMatchesCurrentArch(Path jarFile) {
        String n = jarFile.getFileName().toString().toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean isArm = arch.contains("aarch64") || arch.contains("arm64");
        boolean is64 = arch.contains("64");

        if (n.contains("-x86")) {
            return !is64 && !isArm; // 32-bit only
        }
        if (n.contains("arm64") || n.contains("aarch64")) {
            return isArm;
        }
        // Unqualified "natives-windows" is x86_64 by convention
        if (currentOS() == OS.WINDOWS) {
            return is64 && !isArm;
        }
        return true;
    }
}