package net.coosanta.meldmc.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.coosanta.meldmc.network.data.MeldCodec;
import net.coosanta.meldmc.network.packets.ClientboundModlistResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;

public class MeldPacketReceiver {
    public static void handleCustomPayload(ClientboundCustomPayloadPacket packet) {
        if (!packet.getChannel().equals(MeldCodec.MELD_CHANNEL)) {
            return;
        }

        ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());

        // Packet ID (first byte)
        int packetId = buffer.readUnsignedByte();

        if (packetId == ClientboundModlistResponsePacket.ID) {
            handleModlistResponse(buffer);
        }
    }

    private static void handleModlistResponse(ByteBuf buffer) {
        // Read the HashMap<String, ClientMod> data
        // Based on your Forge packet, it uses buf.writeMap() with writeUtf and writeJsonWithCodec

        var modlistData = new ClientboundModlistResponsePacket(buffer).getModMap();

        // Process the received modlist data
        System.out.println("Received modlist with " + modlistData.size() + " mods:");
        modlistData.forEach((modId, clientMod) -> {
            System.out.println("  " + modId + ": " + clientMod);
        });
    }
}
