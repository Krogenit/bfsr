package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.ship.Ship;

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