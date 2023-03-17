package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.entity.bullet.Bullet;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketSpawnBullet implements PacketOut {
    private int id;
    private String className;
    private Vector2f pos;
    private float sin, cos;
    private int shipId;

    public PacketSpawnBullet(Bullet bullet) {
        this.id = bullet.getId();
        this.className = bullet.getClass().getSimpleName();
        this.pos = bullet.getPosition();
        this.sin = bullet.getSin();
        this.cos = bullet.getCos();
        this.shipId = bullet.getShip().getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        ByteBufUtils.writeString(data, className);
        ByteBufUtils.writeVector(data, pos);
        data.writeFloat(sin);
        data.writeFloat(cos);
        data.writeInt(shipId);
    }
}