package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldRemove implements PacketIn {
    private int id;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ShieldCommon shield = ship.getShield();
            if (shield != null) shield.removeShield();
        }
    }
}