package net.bfsr.server.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketCameraPosition;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketCameraPositionHandler extends PacketHandler<PacketCameraPosition, PlayerNetworkHandler> {
    @Override
    public void handle(PacketCameraPosition packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        playerNetworkHandler.getPlayer().setPosition(packet.getX(), packet.getY());
    }
}