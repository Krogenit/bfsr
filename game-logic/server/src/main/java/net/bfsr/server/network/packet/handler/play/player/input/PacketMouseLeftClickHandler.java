package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.input.PacketMouseLeftClick;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketMouseLeftClickHandler extends PacketHandler<PacketMouseLeftClick, PlayerNetworkHandler> {
    @Override
    public void handle(PacketMouseLeftClick packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().mouseLeftClick();
    }
}