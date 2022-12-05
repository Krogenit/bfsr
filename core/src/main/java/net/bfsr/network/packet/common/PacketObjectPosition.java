package net.bfsr.network.packet.common;

import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketObjectPosition extends Packet {
    private int id;
    private Vector2f pos;
    private float rot;
    private Vector2f velocity;
    private float angularVelocity;

    public PacketObjectPosition(CollisionObject obj) {
        this.id = obj.getId();
        this.pos = obj.getPosition();
        this.rot = obj.getRotation();
        this.velocity = obj.getVelocity();
        this.angularVelocity = obj.getAngularVelocity();
    }

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        pos = data.readVector2f();
        rot = data.readFloat();
        velocity = data.readVector2f();
        angularVelocity = data.readFloat();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeVector2f(pos);
        data.writeFloat(rot);
        data.writeVector2f(velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core core = Core.getCore();
        CollisionObject obj = core.getWorld().getEntityById(id);
        if (obj != null) {
            obj.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);
        } else {
            core.sendPacket(new PacketNeedObjectInfo(id));
        }
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        CollisionObject obj = world.getEntityById(id);
        if (obj != null) {
            Ship s = (Ship) obj;
            s.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);
        }
    }
}