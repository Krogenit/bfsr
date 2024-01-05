package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketRegisterTCP;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketRegisterTCPHandler extends PacketHandler<PacketRegisterTCP, PlayerNetworkHandler> {
    @Override
    public void handle(PacketRegisterTCP packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketRegisterTCP(playerNetworkHandler.getConnectionId()));
    }
}