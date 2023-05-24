package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.ship.PacketDestroyingShip;

import java.net.InetSocketAddress;

public class PacketDestroyingShipHandler extends PacketHandler<PacketDestroyingShip, NetworkSystem> {
    @Override
    public void handle(PacketDestroyingShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            ship.setDestroying();
        }
    }
}