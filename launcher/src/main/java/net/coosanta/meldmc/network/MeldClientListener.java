package net.coosanta.meldmc.network;

import net.coosanta.meldmc.network.handlers.MeldPacketReceiver;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.ClientListener;
import org.geysermc.mcprotocollib.protocol.data.handshake.HandshakeIntent;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.jetbrains.annotations.NotNull;

public class MeldClientListener extends ClientListener {
    public MeldClientListener(@NotNull HandshakeIntent handshakeIntent) {
        super(handshakeIntent);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundCustomPayloadPacket) {
            MeldPacketReceiver.handleCustomPayload((ClientboundCustomPayloadPacket) packet);
        }
    }
}
