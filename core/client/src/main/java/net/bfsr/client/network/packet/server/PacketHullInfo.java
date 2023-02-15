package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketHullInfo implements PacketIn {
    private int id;
    private float hull;

    public PacketHullInfo(ShipCommon ship) {
        this.id = ship.getId();
        this.hull = ship.getHull().getHull();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        hull = data.readFloat();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            ship.getHull().setHull(hull);
        }
    }
}