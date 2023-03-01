package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketOut;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot implements PacketIn, PacketOut {
    private int id;
    private int slot;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        slot = data.readInt();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeInt(slot);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
            weaponSlot.tryShoot();
        }
    }
}