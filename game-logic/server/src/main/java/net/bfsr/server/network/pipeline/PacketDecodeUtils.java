package net.bfsr.server.network.pipeline;

import io.netty.buffer.ByteBuf;
import net.bfsr.network.packet.Packet;
import net.bfsr.server.ServerGameLogic;

import java.io.IOException;

public final class PacketDecodeUtils {
    public static Packet decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            Packet packet = ServerGameLogic.getInstance().getNetworkSystem().createPacket(packetId);
            packet.read(buffer);
            return packet;
        } catch (Exception e) {
            throw new IOException("Can't decode packet with id " + packetId, e);
        }
    }
}