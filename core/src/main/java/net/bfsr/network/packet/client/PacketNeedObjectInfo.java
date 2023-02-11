package net.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.particle.ShipWreck;
import net.bfsr.client.particle.Wreck;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.packet.common.PacketShipEngine;
import net.bfsr.network.packet.server.*;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo extends ClientPacket {
    private int objectId;

    @Override
    public void read(PacketBuffer data) throws IOException {
        objectId = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(objectId);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        CollisionObject obj = world.getEntityById(objectId);
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

                if (ship.isControlledByPlayer() && ship.getOwner() == player) {
                    networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof Bullet bullet) {
                networkManager.scheduleOutboundPacket(new PacketSpawnBullet(bullet));
            } else if (obj instanceof ShipWreck shipWreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnParticle(shipWreck));
            } else if (obj instanceof Wreck wreck) {
                networkManager.scheduleOutboundPacket(new PacketSpawnParticle(wreck));
            }
        }
    }
}