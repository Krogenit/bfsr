package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

@NoArgsConstructor
public class PacketObjectPosition implements PacketIn, PacketOut {
    private int id;
    private Vector2f position;
    private float rotation;
    private Vector2f velocity;
    private float angularVelocity;

    public PacketObjectPosition(CollisionObject obj) {
        this.id = obj.getId();
        this.position = obj.getPosition();
        this.rotation = obj.getRotation();
        this.velocity = obj.getVelocity();
        this.angularVelocity = obj.getAngularVelocity();
    }

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        ByteBufUtils.readVector(data, position = new Vector2f());
        rotation = data.readFloat();
        ByteBufUtils.readVector(data, velocity = new Vector2f());
        angularVelocity = data.readFloat();
    }

    @Override
    public void write(ByteBuf data) {
        data.writeInt(id);
        ByteBufUtils.writeVector(data, position);
        data.writeFloat(rotation);
        ByteBufUtils.writeVector(data, velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnClientSide() {
        Core core = Core.get();
        GameObject obj = core.getWorld().getEntityById(id);
        if (obj != null) {
            obj.updateClientPositionFromPacket(position, rotation, velocity, angularVelocity);
        } else {
            core.sendUDPPacket(new PacketNeedObjectInfo(id));
        }
    }
}