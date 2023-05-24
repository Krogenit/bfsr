package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketShipInfo extends PacketAdapter {
    private int id;
    private float[] armor;
    private int crew;
    private float hull;
    private float energy;
    private float shield;

    public PacketShipInfo(Ship ship) {
        this.id = ship.getId();

        Armor armor = ship.getArmor();
        ArmorPlate[] plates = armor.getArmorPlates();
        this.armor = new float[plates.length];
        for (int i = 0; i < plates.length; i++) {
            if (plates[i] != null) {
                this.armor[i] = plates[i].getArmor();
            }
        }

        this.crew = ship.getCrew() != null ? ship.getCrew().getCrewSize() : 0;
        this.hull = ship.getHull().getHull();
        this.energy = ship.getReactor().getEnergy();
        this.shield = ship.getShield() != null ? ship.getShield().getShield() : 0;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);

        data.writeInt(armor.length);
        for (int i = 0; i < armor.length; i++) {
            float v = armor[i];
            data.writeFloat(v);
        }

        data.writeInt(crew);
        data.writeFloat(hull);
        data.writeFloat(energy);
        data.writeFloat(shield);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();

        armor = new float[data.readInt()];
        for (int i = 0; i < armor.length; i++) {
            armor[i] = data.readFloat();
        }

        crew = data.readInt();
        hull = data.readFloat();
        energy = data.readFloat();
        shield = data.readFloat();
    }
}