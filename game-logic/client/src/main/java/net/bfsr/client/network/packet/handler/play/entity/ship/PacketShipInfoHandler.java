package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;

import java.net.InetSocketAddress;

public class PacketShipInfoHandler extends PacketHandler<PacketShipInfo, NetworkSystem> {
    @Override
    public void handle(PacketShipInfo packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Armor shipArmor = ship.getArmor();
            ArmorPlate[] plates = shipArmor.getArmorPlates();
            float[] armor = packet.getArmor();
            for (int i = 0; i < plates.length; i++) {
                if (plates[i] != null) {
                    plates[i].setArmor(armor[i]);
                }
            }
            ship.getCrew().setCrewSize(packet.getCrew());
            ship.getReactor().setEnergy(packet.getEnergy());
            ship.getHull().setHull(packet.getHull());
            Shield shipShield = ship.getShield();
            if (shipShield != null) shipShield.setShield(packet.getShield());
        } else {
            Core.get().sendUDPPacket(new PacketNeedObjectInfo(packet.getId()));
        }
    }
}