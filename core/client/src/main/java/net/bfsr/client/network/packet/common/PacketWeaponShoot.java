package net.bfsr.client.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot implements PacketIn, PacketOut {
    private int id;
    private int slot;

    @Override
    public void read(PacketBuffer data) {
        id = data.readInt();
        slot = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) {
        data.writeInt(id);
        data.writeInt(slot);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            WeaponSlotCommon weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.clientShoot();
        }
    }
}