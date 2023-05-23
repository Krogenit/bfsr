package net.bfsr.client.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

public class PacketArmorInfo implements PacketIn {
    private int id;
    private float armorValue;
    private int armorPlateId;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        armorValue = data.readFloat();
        armorPlateId = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        GameObject obj = world.getEntityById(id);
        if (obj instanceof Ship ship) {
            Armor armor = ship.getArmor();
            if (armor != null) {
                ArmorPlate plate = armor.getArmorPlate(armorPlateId);
                if (plate != null) {
                    plate.setArmor(armorValue);
                }
            }
        }
    }
}