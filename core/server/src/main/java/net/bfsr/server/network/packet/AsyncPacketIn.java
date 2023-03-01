package net.bfsr.server.network.packet;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public interface AsyncPacketIn extends PacketIn {
    void processOnServerSide(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress);

    @Override
    default void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {

    }

    @Override
    default void handle(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        processOnServerSide(playerNetworkHandler, ctx, remoteAddress);
    }
}
