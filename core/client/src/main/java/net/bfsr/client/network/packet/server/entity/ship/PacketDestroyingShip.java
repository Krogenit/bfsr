package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketDestroyingShip implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setDestroying();
        }
    }
}