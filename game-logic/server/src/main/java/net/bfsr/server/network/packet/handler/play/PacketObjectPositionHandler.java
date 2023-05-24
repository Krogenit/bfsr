package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketObjectPositionHandler extends PacketHandler<PacketObjectPosition, PlayerNetworkHandler> {
    @Override
    public void handle(PacketObjectPosition packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            ship.updateServerPositionFromPacket(packet.getPosition(), packet.getSin(), packet.getCos(), packet.getVelocity(), packet.getAngularVelocity());
        }
    }
}