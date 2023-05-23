package net.bfsr.server.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketArmorInfo implements PacketOut {
    private int id;
    private float armorValue;
    private int armorPlateId;

    public PacketArmorInfo(Ship ship, ArmorPlate plate) {
        this.id = ship.getId();
        this.armorValue = plate.getArmor();
        this.armorPlateId = plate.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeFloat(armorValue);
        data.writeInt(armorPlateId);
    }
}