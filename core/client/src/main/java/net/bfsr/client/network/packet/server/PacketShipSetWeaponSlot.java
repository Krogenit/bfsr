package net.bfsr.client.network.packet.server;

import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class PacketShipSetWeaponSlot implements PacketIn {
    private int id;
    private String slot;
    private int slotId;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        slot = data.readStringFromBuffer(2048);
        slotId = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            try {
                Class<?> clazz = Class.forName("net.bfsr.client.component.weapon." + slot);
                Constructor<?> ctr = clazz.getConstructor(Ship.class);
                WeaponSlotCommon slot = (WeaponSlotCommon) ctr.newInstance(ship);
                ship.addWeaponToSlot(slotId, slot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}