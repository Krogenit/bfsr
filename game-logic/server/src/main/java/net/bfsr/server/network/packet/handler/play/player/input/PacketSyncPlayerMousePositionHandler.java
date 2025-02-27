package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.input.PacketSyncPlayerMousePosition;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketSyncPlayerMousePositionHandler extends PacketHandler<PacketSyncPlayerMousePosition, PlayerNetworkHandler> {
    @Override
    public void handle(PacketSyncPlayerMousePosition packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().setMousePosition(packet.getMousePosition());
    }
}