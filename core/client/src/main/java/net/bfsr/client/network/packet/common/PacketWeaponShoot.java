package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.component.weapon.WeaponSlot;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketOut;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot implements PacketIn, PacketOut {
    private int id;
    private int slot;

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        slot = data.readInt();
    }

    @Override
    public void write(ByteBuf data) {
        data.writeInt(id);
        data.writeInt(slot);
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.clientShoot();
        }
    }
}