package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketShipInfo extends PacketAdapter {
    private int id;
    private float[][] armor;
    private int crew;
    private float[][] hull;
    private float energy;
    private float shield;

    public PacketShipInfo(Ship ship) {
        this.id = ship.getId();

        Modules modules = ship.getModules();

        Hull hull = modules.getHull();
        HullCell[][] hullCells = hull.getCells();
        this.hull = new float[hullCells.length][hullCells[0].length];
        for (int i = 0; i < hullCells.length; i++) {
            for (int j = 0; j < hullCells[0].length; j++) {
                if (hullCells[i][j] != null) {
                    this.hull[i][j] = hullCells[i][j].getValue();
                }
            }
        }

        Armor armor = modules.getArmor();
        ArmorPlate[][] plates = armor.getCells();
        this.armor = new float[plates.length][plates[0].length];
        for (int i = 0; i < plates.length; i++) {
            for (int j = 0; j < plates[0].length; j++) {
                if (plates[i][j] != null) {
                    this.armor[i][j] = plates[i][j].getValue();
                }
            }
        }

        this.crew = modules.getCrew() != null ? modules.getCrew().getCrewSize() : 0;
        this.energy = modules.getReactor().getEnergy();
        this.shield = modules.getShield() != null ? modules.getShield().getShield() : 0;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);

        data.writeShort(hull.length);
        data.writeShort(hull[0].length);
        for (int i = 0; i < hull.length; i++) {
            for (int j = 0; j < hull[0].length; j++) {
                data.writeFloat(hull[i][j]);
            }
        }

        data.writeShort(armor.length);
        data.writeShort(armor[0].length);
        for (int i = 0; i < armor.length; i++) {
            for (int j = 0; j < armor[0].length; j++) {
                data.writeFloat(armor[i][j]);
            }
        }

        data.writeInt(crew);
        data.writeFloat(energy);
        data.writeFloat(shield);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();

        int width = data.readShort();
        int height = data.readShort();
        hull = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                hull[i][j] = data.readFloat();
            }
        }

        width = data.readShort();
        height = data.readShort();
        armor = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                armor[i][j] = data.readFloat();
            }
        }

        crew = data.readInt();
        energy = data.readFloat();
        shield = data.readFloat();
    }
}