package net.bfsr.client.network.pipeline;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;

import java.io.IOException;

public final class PacketDecodeUtils {
    public static PacketIn decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            PacketIn packet = Core.get().getNetworkSystem().createPacket(packetId);
            packet.read(buffer);
            return packet;
        } catch (IOException e) {
            throw new IOException("Can't read packet with id " + packetId, e);
        } catch (Exception e) {
            throw new IOException("Can't create packet with id " + packetId, e);
        }
    }
}