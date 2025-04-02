package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketMouseLeftRelease;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketMouseLeftReleaseHandler extends PacketHandler<PacketMouseLeftRelease, PlayerNetworkHandler> {
    @Override
    public void handle(PacketMouseLeftRelease packet, PlayerNetworkHandler netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        netHandler.getPlayer().getPlayerInputController().mouseLeftRelease();
    }
}