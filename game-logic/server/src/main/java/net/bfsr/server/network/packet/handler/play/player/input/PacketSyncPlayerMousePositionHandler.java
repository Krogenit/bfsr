package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketMouseSyncPosition;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketSyncPlayerMousePositionHandler extends PacketHandler<PacketMouseSyncPosition, PlayerNetworkHandler> {
    @Override
    public void handle(PacketMouseSyncPosition packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().setMousePosition(packet.getMousePosition());
    }
}