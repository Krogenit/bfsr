package net.bfsr.client.network.pipeline;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.Client;
import net.bfsr.network.packet.Packet;

import java.io.IOException;

public final class PacketDecodeUtils {
    public static Packet decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            Packet packet = Client.get().getNetworkSystem().createPacket(packetId);
            packet.read(buffer);
            return packet;
        } catch (IOException e) {
            throw new IOException("Can't read packet with id " + packetId, e);
        } catch (Exception e) {
            throw new IOException("Can't create packet with id " + packetId, e);
        }
    }
}