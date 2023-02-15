package net.bfsr.client.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor
public class PacketSpawnBullet implements PacketIn {
    private int id;
    private String className;
    private Vector2f pos;
    private float sin, cos;
    private int shipId;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        className = data.readStringFromBuffer(1024);
        pos = data.readVector2f();
        sin = data.readFloat();
        cos = data.readFloat();
        shipId = data.readInt();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        if (Core.get().getWorld().getEntityById(id) == null) {
            try {
                WorldClient world = Core.get().getWorld();
                CollisionObject obj = world.getEntityById(shipId);
                if (obj instanceof ShipCommon ship) {
                    Class<?> clazz = Class.forName("net.bfsr.client.entity.bullet." + className);
                    Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, float.class, float.class, float.class, ShipCommon.class);
                    ctr.newInstance(world, id, sin, cos, pos.x, pos.y, ship);
                }
            } catch (ClassNotFoundException | InvocationTargetException | SecurityException | NoSuchMethodException | InstantiationException | IllegalArgumentException
                     | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}