package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.ConnectionState;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketHandshake;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketHandshakeHandler extends PacketHandler<PacketHandshake, PlayerNetworkHandler> {
    @Override
    public void handle(PacketHandshake packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkHandler.setConnectionState(ConnectionState.LOGIN);
        networkHandler.setHandshakeClientTime(packet.getHandshakeClientTime());
        networkHandler.setLoginStartTime(System.currentTimeMillis());
    }
}