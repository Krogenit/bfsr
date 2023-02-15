package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldInfo implements PacketIn {
    private int id;
    private float shieldValue;

    public PacketShieldInfo(ShipCommon ship) {
        this.id = ship.getId();
        this.shieldValue = ship.getShield().getShield();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        shieldValue = data.readFloat();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            ShieldCommon shield = ship.getShield();
            if (shield != null) {
                shield.setShield(shieldValue);
            }
        }
    }
}