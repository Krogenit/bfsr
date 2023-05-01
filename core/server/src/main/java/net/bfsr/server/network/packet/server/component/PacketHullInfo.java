package net.bfsr.server.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketHullInfo implements PacketOut {
    private int id;
    private float hull;

    public PacketHullInfo(Ship ship) {
        this.id = ship.getId();
        this.hull = ship.getHull().getHull();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeFloat(hull);
    }
}