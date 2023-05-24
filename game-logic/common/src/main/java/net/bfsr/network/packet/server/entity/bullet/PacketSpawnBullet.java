package net.bfsr.network.packet.server.entity.bullet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSpawnBullet extends PacketAdapter {
    private int id;
    private int dataIndex;
    private Vector2f pos;
    private float sin, cos;
    private int shipId;

    public PacketSpawnBullet(Bullet bullet) {
        this.id = bullet.getId();
        this.dataIndex = bullet.getBulletData().getDataIndex();
        this.pos = bullet.getPosition();
        this.sin = bullet.getSin();
        this.cos = bullet.getCos();
        this.shipId = bullet.getShip().getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeShort(dataIndex);
        ByteBufUtils.writeVector(data, pos);
        data.writeFloat(sin);
        data.writeFloat(cos);
        data.writeInt(shipId);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        dataIndex = data.readShort();
        ByteBufUtils.readVector(data, pos = new Vector2f());
        sin = data.readFloat();
        cos = data.readFloat();
        shipId = data.readInt();
    }
}