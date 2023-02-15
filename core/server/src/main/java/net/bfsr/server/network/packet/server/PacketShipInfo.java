package net.bfsr.server.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipInfo implements PacketOut {
    private int id;
    private float[] armor;
    private int crew;
    private float hull;
    private float energy;
    private float shield;

    public PacketShipInfo(ShipCommon ship) {
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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);

        data.writeInt(armor.length);
        for (float v : armor) {
            data.writeFloat(v);
        }

        data.writeInt(crew);
        data.writeFloat(hull);
        data.writeFloat(energy);
        data.writeFloat(shield);
    }
}