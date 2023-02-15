package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetPlayerShip implements PacketIn {
    private int id;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        CollisionObject obj = world.getEntityById(id);
        if (obj instanceof Ship ship) {
            world.setPlayerShip(ship);
        }
    }
}