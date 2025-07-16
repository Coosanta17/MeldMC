package net.coosanta.meldmc.network.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.coosanta.meldmc.network.data.MeldCodec;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;

public class ServerboundModlistRequestPacket implements MinecraftPacket {
    private static final int ID = 0x00;

    public ServerboundModlistRequestPacket() {
    }

    @Override
    public void serialize(ByteBuf out) {
    }

    public static void sendInMeldChannel(Session session) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeByte(ID);
            
//            var packet = new ServerboundModlistRequestPacket();
//            packet.serialize(buffer);

            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);

            var customPacket = new ServerboundCustomPayloadPacket(MeldCodec.MELD_CHANNEL, data);
            session.send(customPacket);
        } finally {
            buffer.release();
        }
    }
}
