package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponSlotShoot;

import java.net.InetSocketAddress;

public class PacketWeaponShootHandler extends PacketHandler<PacketWeaponSlotShoot, NetworkSystem> {
    @Override
    public void handle(PacketWeaponSlotShoot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = Client.get().getWorld();
        RigidBody obj = world.getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            WeaponSlot weaponSlot = ship.getWeaponSlot(packet.getSlot());
            if (weaponSlot != null) {
                weaponSlot.shoot(weaponSlot1 -> {}, ship.getModules().getReactor());
            }
        }
    }
}