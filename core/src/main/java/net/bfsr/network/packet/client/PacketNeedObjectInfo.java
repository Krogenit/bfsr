package net.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.particle.ParticleWreck;
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
            if (obj instanceof Ship) {
                Ship ship = (Ship) obj;
                networkManager.scheduleOutboundPacket(new PacketSpawnShip(ship));
                networkManager.scheduleOutboundPacket(new PacketShipName(ship));
                networkManager.scheduleOutboundPacket(new PacketShipFaction(ship));
                Direction dir = ship.getLastMoveDir();
                if (dir != null) networkManager.scheduleOutboundPacket(new PacketShipEngine(objectId, dir.ordinal()));
                for (WeaponSlot slot : ship.getWeaponSlots()) {
                    if (slot != null) networkManager.scheduleOutboundPacket(new PacketShipSetWeaponSlot(ship, slot));
                }

                if (ship.isControlledByPlayer() && ship.getOwner() == player) {
                    networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof Bullet) {
                Bullet b = (Bullet) obj;
                networkManager.scheduleOutboundPacket(new PacketSpawnBullet(b));
            } else if (obj instanceof ParticleWreck) {
                ParticleWreck p = (ParticleWreck) obj;
                networkManager.scheduleOutboundPacket(new PacketSpawnParticle(p));
            }
        }
    }
}