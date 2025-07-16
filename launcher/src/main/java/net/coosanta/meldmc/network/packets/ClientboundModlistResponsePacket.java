package net.coosanta.meldmc.network.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.coosanta.meldmc.minecraft.ClientMod;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClientboundModlistResponsePacket implements MinecraftPacket {
    public static final int ID = 0x01;
    private final @NotNull Map<String, ClientMod> modMap;
    private static final Gson GSON = new GsonBuilder().create();

    public ClientboundModlistResponsePacket(ByteBuf in) {
        modMap = readMap(in);
    }

    private static HashMap<String, ClientMod> readMap(ByteBuf buffer) {
        HashMap<String, ClientMod> map = new HashMap<>();

        int size = MinecraftTypes.readVarInt(buffer);

        for (int i = 0; i < size; i++) {
            String key = MinecraftTypes.readString(buffer);
            ClientMod value = readClientModFromJson(buffer);

            map.put(key, value);
        }

        return map;
    }

    private static ClientMod readClientModFromJson(ByteBuf buffer) {
        String jsonString = MinecraftTypes.readString(buffer);
        JsonObject jsonData = GSON.fromJson(jsonString, JsonObject.class);

        String hash;
        if (jsonData.has("hash")) {
            hash = jsonData.get("hash").getAsString();
        } else {
            throw new IllegalArgumentException("Missing required \"hash\" field in mod data");
        }

        String url = null;
        if (jsonData.has("url")) {
            url = jsonData.get("url").getAsString();
        }

        ClientMod.ModSource modSource = ClientMod.ModSource.UNKNOWN; // TODO: Remove MODSOURCE bs

        String fileName = null;
        if (jsonData.has("fileName")) {
            fileName = jsonData.get("fileName").getAsString();
        }

        return new ClientMod(hash, modSource, url, fileName);
    }

    @Override
    public void serialize(ByteBuf out) {
        throw new UnsupportedOperationException("Cannot serialize ClientboundModlistResponsePacket from MCProtocolLib.");
    }

    public @NotNull Map<String, ClientMod> getModMap() {
        return modMap;
    }
}
