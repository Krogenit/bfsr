package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

@NoArgsConstructor
public class PacketObjectPosition implements PacketIn, PacketOut {
    private int id;
    private Vector2f position;
    private float sin, cos;
    private Vector2f velocity;
    private float angularVelocity;

    public PacketObjectPosition(RigidBody obj) {
        this.id = obj.getId();
        this.position = obj.getPosition();
        this.sin = obj.getSin();
        this.cos = obj.getCos();
        this.velocity = obj.getVelocity();
        this.angularVelocity = obj.getAngularVelocity();
    }

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        ByteBufUtils.readVector(data, position = new Vector2f());
        sin = data.readFloat();
        cos = data.readFloat();
        ByteBufUtils.readVector(data, velocity = new Vector2f());
        angularVelocity = data.readFloat();
    }

    @Override
    public void write(ByteBuf data) {
        data.writeInt(id);
        ByteBufUtils.writeVector(data, position);
        data.writeFloat(sin);
        data.writeFloat(cos);
        ByteBufUtils.writeVector(data, velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnClientSide() {
        Core core = Core.get();
        RigidBody obj = core.getWorld().getEntityById(id);
        if (obj != null) {
            obj.updateClientPositionFromPacket(position, sin, cos, velocity, angularVelocity);
        } else {
            core.sendUDPPacket(new PacketNeedObjectInfo(id));
        }
    }
}