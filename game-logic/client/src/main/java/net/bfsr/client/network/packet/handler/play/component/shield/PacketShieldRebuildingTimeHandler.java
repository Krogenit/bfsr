package net.bfsr.client.network.packet.handler.play.component.shield;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketShieldRebuildingTime;

import java.net.InetSocketAddress;

public class PacketShieldRebuildingTimeHandler extends PacketHandler<PacketShieldRebuildingTime, NetworkSystem> {
    @Override
    public void handle(PacketShieldRebuildingTime packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Shield shield = ship.getShield();
            if (shield != null) shield.setRebuildingTime(packet.getTime());
        }
    }
}