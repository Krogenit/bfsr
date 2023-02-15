package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipInfo implements PacketIn {
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
    public void read(PacketBuffer data) throws IOException {
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

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            Armor shipArmor = ship.getArmor();
            ArmorPlate[] plates = shipArmor.getArmorPlates();
            for (int i = 0; i < plates.length; i++) {
                if (plates[i] != null) plates[i].setArmor(armor[i]);
            }
            ship.getCrew().setCrewSize(crew);
            ship.getReactor().setEnergy(energy);
            ship.getHull().setHull(hull);
            ShieldCommon shipShield = ship.getShield();
            if (shipShield != null) shipShield.setShield(shield);
        } else {
            Core.get().sendPacket(new PacketNeedObjectInfo(id));
        }
    }
}