package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName extends ServerPacket {

    private int id;
    private String name;

    public PacketShipName(Ship ship) {
        this.id = ship.getId();
        this.name = ship.getName();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        name = data.readStringFromBuffer(2048);
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeStringToBuffer(name);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj instanceof Ship) {
            Ship ship = (Ship) obj;
            ship.setName(name);
        }
    }
}