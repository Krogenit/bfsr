package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.math.Direction;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.bullet.Bullet;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.wreck.ShipWreck;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import net.bfsr.server.network.packet.common.PacketShipEngine;
import net.bfsr.server.network.packet.server.*;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo implements PacketIn {
    private int objectId;

    @Override
    public void read(PacketBuffer data) throws IOException {
        objectId = data.readInt();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        GameObject obj = networkManager.getWorld().getEntityById(objectId);
        if (obj != null) {
            if (obj instanceof Ship ship) {
                networkManager.scheduleOutboundPacket(new PacketSpawnShip(ship));
                networkManager.scheduleOutboundPacket(new PacketShipName(ship));
                networkManager.scheduleOutboundPacket(new PacketShipFaction(ship));
                Direction dir = ship.getLastMoveDir();
                if (dir != null) networkManager.scheduleOutboundPacket(new PacketShipEngine(objectId, dir.ordinal()));
                List<WeaponSlot> weaponSlots = ship.getWeaponSlots();
                for (int i = 0; i < weaponSlots.size(); i++) {
                    WeaponSlot slot = weaponSlots.get(i);
                    if (slot != null) networkManager.scheduleOutboundPacket(new PacketShipSetWeaponSlot(ship, slot));
                }

                if (ship.isControlledByPlayer() && ship.getOwner() == networkManager.getPlayer()) {
                    networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof Bullet bullet) {
                networkManager.scheduleOutboundPacket(new PacketSpawnBullet(bullet));
            } else if (obj instanceof ShipWreck shipWreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnWreck(shipWreck));
            } else if (obj instanceof Wreck wreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnWreck(wreck));
            }
        }
    }
}