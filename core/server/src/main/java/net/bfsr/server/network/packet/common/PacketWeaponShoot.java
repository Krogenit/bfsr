package net.bfsr.server.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot implements PacketIn, PacketOut {
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
    public void processOnServerSide(NetworkManagerServer networkManager) {
        CollisionObject obj = networkManager.getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            WeaponSlotCommon weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.tryShoot();
        }
    }
}