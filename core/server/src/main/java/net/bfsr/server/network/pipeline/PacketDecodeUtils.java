package net.bfsr.server.network.pipeline;

import io.netty.buffer.ByteBuf;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

public final class PacketDecodeUtils {
    public static PacketIn decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            PacketIn packet = MainServer.getInstance().getNetworkSystem().createPacket(packetId);
            packet.read(buffer);
            return packet;
        } catch (Exception e) {
            throw new IOException("Can't decode packet with id " + packetId, e);
        }
    }
}