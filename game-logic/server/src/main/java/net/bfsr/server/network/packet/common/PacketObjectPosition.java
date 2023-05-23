package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketObjectPosition implements PacketOut, PacketIn {
    private int id;
    private Vector2f pos;
    private float sin, cos;
    private Vector2f velocity;
    private float angularVelocity;

    public PacketObjectPosition(RigidBody obj) {
        this.id = obj.getId();
        this.pos = obj.getPosition();
        this.sin = obj.getSin();
        this.cos = obj.getCos();
        this.velocity = obj.getVelocity();
        this.angularVelocity = obj.getAngularVelocity();
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        ByteBufUtils.readVector(data, pos = new Vector2f());
        sin = data.readFloat();
        cos = data.readFloat();
        ByteBufUtils.readVector(data, velocity = new Vector2f());
        angularVelocity = data.readFloat();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        ByteBufUtils.writeVector(data, pos);
        data.writeFloat(sin);
        data.writeFloat(cos);
        ByteBufUtils.writeVector(data, velocity);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.updateServerPositionFromPacket(pos, sin, cos, velocity, angularVelocity);
        }
    }
}