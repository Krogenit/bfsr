package net.bfsr.server.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.Packet;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface PacketIn extends Packet {
    void read(ByteBuf data) throws IOException;
    void processOnServerSide(PlayerNetworkHandler playerNetworkHandler);
    default void handle(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        playerNetworkHandler.addPacketToQueue(this);
    }
}
