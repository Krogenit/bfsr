package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.input.PacketShipStopMove;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketShipStopMoveHandler extends PacketHandler<PacketShipStopMove, PlayerNetworkHandler> {
    @Override
    public void handle(PacketShipStopMove packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().stopMove(packet.getDirection());
    }
}