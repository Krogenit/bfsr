package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipInfo implements PacketIn {
    private int id;
    private float[] armor;
    private int crew;
    private float hull;
    private float energy;
    private float shield;

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

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            Armor shipArmor = ship.getArmor();
            ArmorPlate[] plates = shipArmor.getArmorPlates();
            for (int i = 0; i < plates.length; i++) {
                if (plates[i] != null) plates[i].setArmor(armor[i]);
            }
            ship.getCrew().setCrewSize(crew);
            ship.getReactor().setEnergy(energy);
            ship.getHull().setHull(hull);
            Shield shipShield = ship.getShield();
            if (shipShield != null) shipShield.setShield(shield);
        } else {
            Core.get().sendUDPPacket(new PacketNeedObjectInfo(id));
        }
    }
}