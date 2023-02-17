package net.bfsr.client.network.packet.server;

import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.network.PacketBuffer;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketSpawnShip implements PacketIn {
    private int id;
    private String shipClassName;
    private Vector2f position;
    private float rot;
    private boolean isSpawned;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.id = data.readInt();
        this.position = data.readVector2f();
        this.rot = data.readFloat();
        this.shipClassName = data.readStringFromBuffer(256);
        this.isSpawned = data.readBoolean();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            try {
                Class<?> clazz = Class.forName("net.bfsr.client.entity.ship." + shipClassName);
                Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, float.class, float.class);
                Ship ship = (Ship) ctr.newInstance(world, id, position.x, position.y, rot);
                ship.init();

                if (isSpawned) ship.setSpawned();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}