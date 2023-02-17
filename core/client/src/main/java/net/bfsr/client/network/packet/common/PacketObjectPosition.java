package net.bfsr.client.network.packet.common;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import org.joml.Vector2f;

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

    public void read(PacketBuffer data) {
        id = data.readInt();
        pos = data.readVector2f();
        rot = data.readFloat();
        velocity = data.readVector2f();
        angularVelocity = data.readFloat();
    }

    public void write(PacketBuffer data) {
        data.writeInt(id);
        data.writeVector2f(pos);
        data.writeFloat(rot);
        data.writeVector2f(velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core core = Core.get();
        GameObject obj = core.getWorld().getEntityById(id);
        if (obj != null) {
            obj.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);
        } else {
            core.sendPacket(new PacketNeedObjectInfo(id));
        }
    }
}