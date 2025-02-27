package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.input.PacketShipMove;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketShipMoveHandler extends PacketHandler<PacketShipMove, PlayerNetworkHandler> {
    @Override
    public void handle(PacketShipMove packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().move(packet.getDirection());
    }
}