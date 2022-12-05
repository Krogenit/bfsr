package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.particle.Particle;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRemoveObject extends ServerPacket {
    private int id;

    public PacketRemoveObject(CollisionObject obj) {
        this.id = obj.getId();
    }

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj != null) {
            if (obj instanceof Ship) {
                ((Ship) obj).destroyShip();
            } else if (obj instanceof Bullet) {
                obj.setDead(true);
            } else if (obj instanceof Particle) {
                Particle p = (Particle) obj;
                p.setDead(true);
            }
        }
    }
}