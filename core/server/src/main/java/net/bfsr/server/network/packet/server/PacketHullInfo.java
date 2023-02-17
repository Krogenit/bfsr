package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.ship.Ship;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketHullInfo implements PacketOut {
    private int id;
    private float hull;

    public PacketHullInfo(Ship ship) {
        this.id = ship.getId();
        this.hull = ship.getHull().getHull();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeFloat(hull);
    }
}