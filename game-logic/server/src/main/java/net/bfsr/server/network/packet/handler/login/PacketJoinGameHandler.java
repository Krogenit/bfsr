package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.server.login.PacketJoinGame;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketJoinGameHandler extends PacketHandler<PacketJoinGame, PlayerNetworkHandler> {
    @Override
    public void handle(PacketJoinGame packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkHandler.joinGame();
    }
}