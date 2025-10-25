package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.ConnectionState;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.client.PacketHandshake;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketHandshakeHandler extends PacketHandler<PacketHandshake, PlayerNetworkHandler> {
    @Override
    public void handle(PacketHandshake packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkHandler.setConnectionState(ConnectionState.LOGIN);
        networkHandler.setLoginStartTime(System.currentTimeMillis());
    }
}