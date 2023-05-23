package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

public class PacketShipSetSpawned implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject gameObject = Core.get().getWorld().getEntityById(id);
        if (gameObject instanceof Ship ship) {
            ship.setSpawned();
        }
    }
}