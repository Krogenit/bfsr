package net.bfsr.client.network.packet.server.entity.bullet;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.config.bullet.BulletData;
import net.bfsr.config.bullet.BulletRegistry;
import net.bfsr.entity.GameObject;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketSpawnBullet implements PacketIn {
    private int id;
    private int index;
    private Vector2f pos;
    private float sin, cos;
    private int shipId;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        index = data.readShort();
        ByteBufUtils.readVector(data, pos = new Vector2f());
        sin = data.readFloat();
        cos = data.readFloat();
        shipId = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        GameObject obj = world.getEntityById(shipId);
        if (obj instanceof Ship ship) {
            BulletData bulletData = BulletRegistry.INSTANCE.get(index);
            Bullet bullet = new Bullet(world, id, pos.x, pos.y, sin, cos, ship, bulletData);
            bullet.init();
            world.addBullet(bullet);
        }
    }
}