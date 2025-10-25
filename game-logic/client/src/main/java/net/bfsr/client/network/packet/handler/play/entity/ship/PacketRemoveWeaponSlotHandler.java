package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponSlotRemove;

import java.net.InetSocketAddress;

public class PacketRemoveWeaponSlotHandler extends PacketHandler<PacketWeaponSlotRemove, NetworkSystem> {
    @Override
    public void handle(PacketWeaponSlotRemove packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        RigidBody obj = Client.get().getWorld().getEntityById(packet.getShipId());
        if (obj instanceof Ship ship) {
            WeaponSlot weaponSlot = ship.getWeaponSlot(packet.getSlotId());
            ship.removeConnectedObject(weaponSlot);
        }
    }
}