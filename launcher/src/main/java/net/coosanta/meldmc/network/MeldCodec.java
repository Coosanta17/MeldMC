package net.coosanta.meldmc.network;

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
    public static final PacketCodec CODEC = PacketCodec.builder()
            .protocolVersion(771)
            .minecraftVersion("1.21.6")
            .state(ProtocolState.HANDSHAKE, MinecraftPacketRegistry.builder()
                    .registerServerboundPacket(ClientIntentionPacket.class, ClientIntentionPacket::new)
            )
            .state(ProtocolState.STATUS, MinecraftPacketRegistry.builder()
                    .registerClientboundPacket(MeldStatusResponsePacket.class, MeldStatusResponsePacket::new)
                    .registerClientboundPacket(ClientboundPongResponsePacket.class, ClientboundPongResponsePacket::new)
                    .registerServerboundPacket(ServerboundStatusRequestPacket.class, ServerboundStatusRequestPacket::new)
                    .registerServerboundPacket(ServerboundPingRequestPacket.class, ServerboundPingRequestPacket::new)
            )
            .build();
}
