package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;

import java.net.InetSocketAddress;

public class PacketWeaponShootHandler extends PacketHandler<PacketWeaponShoot, NetworkSystem> {
    @Override
    public void handle(PacketWeaponShoot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        RigidBody obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            ship.getWeaponSlot(packet.getSlot()).shoot();
        }
    }
}