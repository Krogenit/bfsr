package net.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot extends Packet {

    private int id;
    private int slot;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        slot = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(slot);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj instanceof Ship) {
            Ship ship = (Ship) obj;
            WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.clientShoot();
        }
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        CollisionObject obj = world.getEntityById(id);
        if (obj instanceof Ship) {
            Ship ship = (Ship) obj;
            WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.shoot();
        }
    }
}