package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName implements PacketIn {
    private int id;
    private String name;

    public PacketShipName(ShipCommon ship) {
        this.id = ship.getId();
        this.name = ship.getName();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        name = data.readStringFromBuffer(2048);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            ship.setName(name);
        }
    }
}