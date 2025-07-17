package net.coosanta.meldmc.network.data;

import net.coosanta.meldmc.network.packets.MeldClientboundStatusResponsePacket;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacketRegistry;
import org.geysermc.mcprotocollib.protocol.codec.PacketCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.handshake.serverbound.ClientIntentionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ping.clientbound.ClientboundPongResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.ping.serverbound.ServerboundPingRequestPacket;
import org.geysermc.mcprotocollib.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;

/**
 * MeldCodec defines a custom protocol implementation for Meld-specific data.
 * Extension of the Minecraft protocol because otherwise it cannot connect to
 * Minecraft servers!!!
 */
public class MeldCodec {
    public static final Key MELD_CHANNEL = Key.key("meldmc", "main");

    public static final PacketCodec CODEC = PacketCodec.builder()
            .protocolVersion(771)
            .minecraftVersion("1.21.6")
            .state(ProtocolState.HANDSHAKE, MinecraftPacketRegistry.builder()
                    .registerServerboundPacket(ClientIntentionPacket.class, ClientIntentionPacket::new)
            )
            .state(ProtocolState.STATUS, MinecraftPacketRegistry.builder()
                    .registerClientboundPacket(MeldClientboundStatusResponsePacket.class, MeldClientboundStatusResponsePacket::new) // <-- important
                    .registerClientboundPacket(ClientboundPongResponsePacket.class, ClientboundPongResponsePacket::new)

                    .registerServerboundPacket(ServerboundStatusRequestPacket.class, ServerboundStatusRequestPacket::new)
                    .registerServerboundPacket(ServerboundPingRequestPacket.class, ServerboundPingRequestPacket::new)
            )
            .build();
}
