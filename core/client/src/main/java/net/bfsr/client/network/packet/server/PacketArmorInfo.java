package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@NoArgsConstructor
public class PacketArmorInfo implements PacketIn {
    private int id;
    private float armorValue;
    private int armorPlateId;

    public PacketArmorInfo(ShipCommon ship, ArmorPlate plate) {
        this.id = ship.getId();
        this.armorValue = plate.getArmor();
        this.armorPlateId = plate.getId();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        armorValue = data.readFloat();
        armorPlateId = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        CollisionObject obj = world.getEntityById(id);
        if (obj instanceof ShipCommon ship) {
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