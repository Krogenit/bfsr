package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRemoveObject implements PacketIn {
    private int id;

    public PacketRemoveObject(CollisionObject obj) {
        this.id = obj.getId();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            ship.destroyShip();
        } else if (obj instanceof BulletCommon) {
            obj.setDead(true);
        } else if (obj instanceof WreckCommon p) {
            p.setDead(true);
        }
    }
}