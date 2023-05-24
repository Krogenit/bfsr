package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketLoginTCP;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketLoginTCPHandler extends PacketHandler<PacketLoginTCP, PlayerNetworkHandler> {
    @Override
    public void handle(PacketLoginTCP packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        playerNetworkHandler.loginTCP(packet.getLogin());
    }
}