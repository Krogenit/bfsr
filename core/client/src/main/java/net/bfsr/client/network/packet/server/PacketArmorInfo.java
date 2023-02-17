package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@NoArgsConstructor
public class PacketArmorInfo implements PacketIn {
    private int id;
    private float armorValue;
    private int armorPlateId;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        armorValue = data.readFloat();
        armorPlateId = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
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