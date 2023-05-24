package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketArmorInfo extends PacketAdapter {
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

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        armorValue = data.readFloat();
        armorPlateId = data.readInt();
    }
}