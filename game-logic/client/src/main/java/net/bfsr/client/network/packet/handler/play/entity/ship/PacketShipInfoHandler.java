package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;

import java.net.InetSocketAddress;

public class PacketShipInfoHandler extends PacketHandler<PacketShipInfo, NetworkSystem> {
    @Override
    public void handle(PacketShipInfo packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Modules modules = ship.getModules();

            Hull hull = modules.getHull();
            HullCell[][] hullCells = hull.getCells();
            float[][] hullValues = packet.getHull();
            for (int i = 0; i < hullCells.length; i++) {
                for (int j = 0; j < hullCells[0].length; j++) {
                    if (hullCells[i][j] != null) {
                        hullCells[i][j].setValue(hullValues[i][j]);
                    }
                }
            }

            Armor shipArmor = modules.getArmor();
            ArmorPlate[][] plates = shipArmor.getCells();
            float[][] armor = packet.getArmor();
            for (int i = 0; i < plates.length; i++) {
                for (int j = 0; j < plates[0].length; j++) {
                    if (plates[i][j] != null) {
                        plates[i][j].setValue(armor[i][j]);
                    }
                }
            }
            modules.getCrew().setCrewSize(packet.getCrew());

            if (!ship.isControlledByPlayer()) //Fixed desync in fire speed
                modules.getReactor().setEnergy(packet.getEnergy());

            Shield shipShield = modules.getShield();
            if (shipShield != null) shipShield.setShieldHp(packet.getShield());
        }
    }
}