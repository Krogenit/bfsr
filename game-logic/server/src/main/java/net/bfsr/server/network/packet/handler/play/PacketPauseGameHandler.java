package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketPauseGame;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketPauseGameHandler extends PacketHandler<PacketPauseGame, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.getInstance();

    @Override
    public void handle(PacketPauseGame packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        gameLogic.setPaused(!gameLogic.isPaused());
    }
}