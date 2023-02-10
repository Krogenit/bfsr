package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetPlayerShip extends ServerPacket {
    private int id;

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        CollisionObject obj = world.getEntityById(id);
        if (obj != null) {
            Ship ship = (Ship) obj;
            world.setPlayerShip(ship);
        }
    }
}