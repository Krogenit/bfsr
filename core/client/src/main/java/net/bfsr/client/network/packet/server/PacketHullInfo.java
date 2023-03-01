package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketHullInfo implements PacketIn {
    private int id;
    private float hull;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        hull = data.readFloat();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.getHull().setHull(hull);
        }
    }
}