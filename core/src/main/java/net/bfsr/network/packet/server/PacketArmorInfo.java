package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;

import java.io.IOException;

@NoArgsConstructor
public class PacketArmorInfo extends ServerPacket {

    private int id;
    private float armorValue;
    private int armorPlateId;

    public PacketArmorInfo(Ship ship, ArmorPlate plate) {
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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeFloat(armorValue);
        data.writeInt(armorPlateId);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        CollisionObject obj = world.getEntityById(id);
        if (obj != null) {
            Ship ship = (Ship) obj;
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