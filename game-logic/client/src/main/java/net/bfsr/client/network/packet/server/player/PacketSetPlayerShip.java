package net.bfsr.client.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

public class PacketSetPlayerShip implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        Core core = Core.get();
        WorldClient world = core.getWorld();
        GameObject obj = world.getEntityById(id);
        if (obj instanceof Ship ship) {
            core.getInputHandler().getPlayerInputController().setShip(ship);
        }
    }
}