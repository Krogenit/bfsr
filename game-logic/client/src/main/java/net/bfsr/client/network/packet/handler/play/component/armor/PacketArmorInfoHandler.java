package net.bfsr.client.network.packet.handler.play.component.armor;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketArmorInfo;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketArmorInfoHandler extends PacketHandler<PacketArmorInfo, NetworkSystem> {
    @Override
    public void handle(PacketArmorInfo packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        World world = Core.get().getWorld();
        GameObject obj = world.getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Armor armor = ship.getArmor();
            if (armor != null) {
                ArmorPlate plate = armor.getArmorPlate(packet.getArmorPlateId());
                if (plate != null) {
                    plate.setArmor(packet.getArmorValue());
                }
            }
        }
    }
}