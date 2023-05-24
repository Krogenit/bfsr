package net.bfsr.server.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketPing;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, PlayerNetworkHandler> {
    @Override
    public void handle(PacketPing packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketPing(System.nanoTime() - (playerNetworkHandler.getHandshakeClientTime() + packet.getTime())));
    }
}