package net.bfsr.server.network.packet.common;

import lombok.NoArgsConstructor;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketObjectPosition implements PacketIn, PacketOut {
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

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        pos = data.readVector2f();
        rot = data.readFloat();
        velocity = data.readVector2f();
        angularVelocity = data.readFloat();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeVector2f(pos);
        data.writeFloat(rot);
        data.writeVector2f(velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        CollisionObject obj = networkManager.getWorld().getEntityById(id);
        if (obj instanceof ShipCommon ship) {
            ship.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);
        }
    }
}