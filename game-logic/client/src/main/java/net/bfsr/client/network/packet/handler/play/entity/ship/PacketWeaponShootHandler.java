package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketWeaponShootHandler extends PacketHandler<PacketWeaponShoot, NetworkSystem> {
    @Override
    public void handle(PacketWeaponShoot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = Core.get().getWorld();
        RigidBody<?> obj = world.getEntityById(packet.getId());
        if (obj instanceof Ship ship && ship != Core.get().getInputHandler().getPlayerInputController().getShip()) {
            WeaponSlot weaponSlot = ship.getWeaponSlot(packet.getSlot());
            if (weaponSlot != null) {
                weaponSlot.shoot(weaponSlot1 -> world.getEventBus().publish(new WeaponShotEvent(weaponSlot1)));
            }
        }
    }
}