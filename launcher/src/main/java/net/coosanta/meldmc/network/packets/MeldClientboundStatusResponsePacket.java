package net.coosanta.meldmc.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.coosanta.meldmc.network.data.MeldServerStatusInfo;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MeldClientboundStatusResponsePacket extends ClientboundStatusResponsePacket {
    // vanilla behavior falls back to false if the field was not sent
    private static final boolean ENFORCES_SECURE_CHAT_DEFAULT = false;
    private static final boolean MELD_SUPPORT_DEFAULT = false;
    private static final int DEFAULT_PORT = 8080;
    private static final Logger log = LoggerFactory.getLogger(MeldClientboundStatusResponsePacket.class);
    private final @NonNull JsonObject jsonData;

    public MeldClientboundStatusResponsePacket(ByteBuf in) {
        super(in);
        this.jsonData = super.getData();
    }

    @Override
    public ServerStatusInfo parseInfo() {
        JsonElement desc = jsonData.get("description");
        Component description = DefaultComponentSerializer.get().deserializeFromTree(desc);

        PlayerInfo players = null;
        if (jsonData.has("players")) {
            JsonObject plrs = jsonData.get("players").getAsJsonObject();
            List<GameProfile> profiles = new ArrayList<>();
            if (plrs.has("sample")) {
                JsonArray prof = plrs.get("sample").getAsJsonArray();
                if (!prof.isEmpty()) {
                    for (int index = 0; index < prof.size(); index++) {
                        JsonObject o = prof.get(index).getAsJsonObject();
                        profiles.add(new GameProfile(o.get("id").getAsString(), o.get("name").getAsString()));
                    }
                }
            }

            players = new PlayerInfo(plrs.get("max").getAsInt(), plrs.get("online").getAsInt(), profiles);
        }

        VersionInfo version = null;
        if (jsonData.has("version")) {
            JsonObject ver = jsonData.get("version").getAsJsonObject();
            version = new VersionInfo(ver.get("name").getAsString(), ver.get("protocol").getAsInt());
        }

        byte[] icon = null;
        if (jsonData.has("favicon")) {
            icon = stringToIcon(jsonData.get("favicon").getAsString());
        }

        boolean enforcesSecureChat = ENFORCES_SECURE_CHAT_DEFAULT;
        if (jsonData.has("enforcesSecureChat")) {
            enforcesSecureChat = jsonData.get("enforcesSecureChat").getAsBoolean();
        }

        boolean meldSupported = MELD_SUPPORT_DEFAULT;
        if (jsonData.has("meldSupport")) {
            meldSupported = jsonData.get("meldSupport").getAsBoolean();
        }

        int port = DEFAULT_PORT;
        if (jsonData.has("meldPort")) {
            port = jsonData.get("meldPort").getAsInt();
        }

        return new MeldServerStatusInfo(description, players, version, icon, enforcesSecureChat, meldSupported, port);
    }
}
