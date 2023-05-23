package net.bfsr.client.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.Packet;

import java.io.IOException;

public interface PacketIn extends Packet {
    void read(ByteBuf data) throws IOException;
    void processOnClientSide();
    default void handle(NetworkSystem networkSystem, ChannelHandlerContext ctx) {
        networkSystem.addPacketToInboundQueue(this);
    }
}
