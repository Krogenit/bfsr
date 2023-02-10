package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.Constructor;

@NoArgsConstructor
public class PacketSpawnBullet extends ServerPacket {

    private int id;
    private String className;
    private Vector2f pos;
    private float rot;
    private int shipId;

    public PacketSpawnBullet(Bullet bullet) {
        this.id = bullet.getId();
        this.className = bullet.getClass().getName();
        this.pos = bullet.getPosition();
        this.rot = bullet.getRotation();
        this.shipId = bullet.getShip().getId();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        className = data.readStringFromBuffer(1024);
        pos = data.readVector2f();
        rot = data.readFloat();
        shipId = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeStringToBuffer(className);
        data.writeVector2f(pos);
        data.writeFloat(rot);
        data.writeInt(shipId);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        if (Core.get().getWorld().getEntityById(id) == null) {
            try {
                WorldClient world = Core.get().getWorld();
                CollisionObject obj = world.getEntityById(shipId);
                if (obj instanceof Ship ship) {
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, float.class, float.class, Ship.class);
                    ctr.newInstance(world, id, rot, pos.x, pos.y, ship);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}