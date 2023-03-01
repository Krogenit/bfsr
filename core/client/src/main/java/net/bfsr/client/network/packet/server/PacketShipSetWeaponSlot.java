package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.component.weapon.WeaponSlot;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketShipSetWeaponSlot implements PacketIn {
    private int id;
    private String slot;
    private int slotId;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        slot = ByteBufUtils.readString(data);
        slotId = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            try {
                Class<?> clazz = Class.forName("net.bfsr.client.component.weapon." + slot);
                Constructor<?> ctr = clazz.getConstructor(Ship.class);
                WeaponSlot slot = (WeaponSlot) ctr.newInstance(ship);
                ship.addWeaponToSlot(slotId, slot);
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}