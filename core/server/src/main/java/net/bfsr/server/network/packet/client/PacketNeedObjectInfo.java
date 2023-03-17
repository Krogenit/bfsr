package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.math.Direction;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.bullet.Bullet;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.wreck.ShipWreck;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.network.packet.common.PacketShipEngine;
import net.bfsr.server.network.packet.server.*;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo implements PacketIn {
    private int objectId;

    @Override
    public void read(ByteBuf data) throws IOException {
        objectId = data.readInt();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(objectId);
        if (obj != null) {
            if (obj instanceof Ship ship) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnShip(ship));
                playerNetworkHandler.sendTCPPacket(new PacketShipName(ship));
                playerNetworkHandler.sendTCPPacket(new PacketShipFaction(ship));
                Direction dir = ship.getLastMoveDir();
                if (dir != null) playerNetworkHandler.sendTCPPacket(new PacketShipEngine(objectId, dir.ordinal()));
                List<WeaponSlot> weaponSlots = ship.getWeaponSlots();
                for (int i = 0; i < weaponSlots.size(); i++) {
                    WeaponSlot slot = weaponSlots.get(i);
                    if (slot != null) playerNetworkHandler.sendTCPPacket(new PacketShipSetWeaponSlot(ship, slot));
                }

                if (ship.isControlledByPlayer() && ship.getOwner() == playerNetworkHandler.getPlayer()) {
                    playerNetworkHandler.sendTCPPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof Bullet bullet) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnBullet(bullet));
            } else if (obj instanceof ShipWreck shipWreck) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnWreck(shipWreck));
            } else if (obj instanceof Wreck wreck) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnWreck(wreck));
            }
        }
    }
}