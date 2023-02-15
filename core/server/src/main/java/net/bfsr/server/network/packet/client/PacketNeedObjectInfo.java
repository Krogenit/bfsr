package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.math.Direction;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.entity.Ship;
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
        CollisionObject obj = networkManager.getWorld().getEntityById(objectId);
        if (obj != null) {
            if (obj instanceof Ship ship) {
                networkManager.scheduleOutboundPacket(new PacketSpawnShip(ship));
                networkManager.scheduleOutboundPacket(new PacketShipName(ship));
                networkManager.scheduleOutboundPacket(new PacketShipFaction(ship));
                Direction dir = ship.getLastMoveDir();
                if (dir != null) networkManager.scheduleOutboundPacket(new PacketShipEngine(objectId, dir.ordinal()));
                List<WeaponSlotCommon> weaponSlots = ship.getWeaponSlots();
                for (int i = 0; i < weaponSlots.size(); i++) {
                    WeaponSlotCommon slot = weaponSlots.get(i);
                    if (slot != null) networkManager.scheduleOutboundPacket(new PacketShipSetWeaponSlot(ship, slot));
                }

                if (ship.isControlledByPlayer() && ship.getOwner() == networkManager.getPlayer()) {
                    networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof BulletCommon bullet) {
                networkManager.scheduleOutboundPacket(new PacketSpawnBullet(bullet));
            } else if (obj instanceof ShipWreck shipWreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnWreckCommon(shipWreck));
            } else if (obj instanceof Wreck wreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnWreckCommon(wreck));
            }
        }
    }
}