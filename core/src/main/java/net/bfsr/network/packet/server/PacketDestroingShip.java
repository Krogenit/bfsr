package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDestroingShip extends ServerPacket {
    private int id;

    public PacketDestroingShip(CollisionObject obj) {
        this.id = obj.getId();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj != null) {
            Ship ship = (Ship) obj;
            ship.setDestroing();
        }
    }
}