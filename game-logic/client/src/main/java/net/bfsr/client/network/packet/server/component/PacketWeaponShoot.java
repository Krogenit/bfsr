package net.bfsr.client.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

public class PacketWeaponShoot implements PacketIn {
    private int id;
    private int slot;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        slot = data.readByte();
    }

    @Override
    public void processOnClientSide() {
        RigidBody obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.getWeaponSlot(slot).shoot();
        }
    }
}