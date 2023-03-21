package net.bfsr.client.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketSetPlayerShip implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        GameObject obj = world.getEntityById(id);
        if (obj instanceof Ship ship) {
            world.setPlayerShip(ship);
        }
    }
}