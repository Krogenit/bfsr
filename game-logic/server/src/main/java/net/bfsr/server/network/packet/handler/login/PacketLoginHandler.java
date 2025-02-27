package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.client.PacketLogin;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketLoginHandler extends PacketHandler<PacketLogin, PlayerNetworkHandler> {
    @Override
    public void handle(PacketLogin packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        playerNetworkHandler.loginTCP(packet.getLogin());
    }
}