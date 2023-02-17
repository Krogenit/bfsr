package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName implements PacketIn {
    private int id;
    private String name;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        name = data.readStringFromBuffer(2048);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setName(name);
        }
    }
}