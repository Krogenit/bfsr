package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketHullInfo implements PacketOut {
    private int id;
    private float hull;

    public PacketHullInfo(ShipCommon ship) {
        this.id = ship.getId();
        this.hull = ship.getHull().getHull();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeFloat(hull);
    }
}