package net.bfsr.server.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketKeepAlive;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketKeepAliveHandler extends PacketHandler<PacketKeepAlive, PlayerNetworkHandler> {
    @Override
    public void handle(PacketKeepAlive packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {}
}