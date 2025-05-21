package net.coosanta.meldmc.gui.serverselection;

import javafx.scene.layout.VBox;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.coosanta.meldmc.Main.*;

public class CentrePanel extends VBox {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExecutorService pingTask = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    private final Path gameDir = Main.getGameDir();

    private CompoundTag serversDat;

    public CentrePanel() {
        setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

        setManaged(true);

        try {
            List<ServerEntry> serverList = getServersFromFile();
            getChildren().addAll(serverList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<ServerEntry> getServersFromFile() throws IOException {
        File serversDatFile = gameDir.resolve("servers.dat").toFile();

        if (!serversDatFile.isFile()) {
            log.info("servers.dat not found, creating new file");
            initializeEmptyServersDat(serversDatFile);
        } else {
            loadServersDat(serversDatFile);
        }

        ListTag<CompoundTag> serversDatList = extractServersList();

        ArrayList<ServerEntry> serverList = new ArrayList<>();
        serversDatList.forEach((server) -> serverList.add(new ServerEntry(new ServerInfo(server), pingTask)));

        return serverList;
    }

    private void initializeEmptyServersDat(File serversDatFile) throws IOException {
        serversDat = new CompoundTag();
        serversDat.put("servers", new ListTag<>(CompoundTag.class));
        NBTUtil.write(serversDat, serversDatFile, false);
    }

    private void loadServersDat(File serversDatFile) throws IOException {
        Tag<?> serversDatRaw = NBTUtil.read(serversDatFile).getTag();
        if (serversDatRaw.getID() != 10) {
            throw new IllegalArgumentException("Invalid tags in servers.dat - Expected Compound");
        }
        serversDat = (CompoundTag) serversDatRaw;
    }

    private ListTag<CompoundTag> extractServersList() {
        Tag<?> serversListRaw = serversDat.get("servers");
        if (serversListRaw == null) {
            log.warn("Missing 'servers' tag in servers.dat, creating empty list");
            ListTag<CompoundTag> emptyList = new ListTag<>(CompoundTag.class);
            serversDat.put("servers", emptyList);
            return emptyList;
        }

        if (serversListRaw.getID() != 9) {
            throw new IllegalArgumentException("Invalid tags in servers.dat root compound tag - Expected List");
        }

        ListTag<?> serversListUnchecked = (ListTag<?>) serversListRaw;
        if (serversListUnchecked.getTypeClass() != CompoundTag.class) {
            throw new IllegalArgumentException("Invalid tags in servers.dat List - Expected Compound");
        }

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> serversList = (ListTag<CompoundTag>) serversListUnchecked;
        return serversList;
    }
}
