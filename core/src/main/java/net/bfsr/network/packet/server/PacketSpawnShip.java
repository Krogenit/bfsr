package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor
@Log4j2
public class PacketSpawnShip extends ServerPacket {
    private int id;
    private String shipClassName;
    private Vector2f position;
    private float rot;
    private boolean isSpawned;

    public PacketSpawnShip(Ship ship) {
        this.id = ship.getId();
        this.position = ship.getPosition();
        this.rot = ship.getRotation();
        this.shipClassName = ship.getClass().getName();
        this.isSpawned = ship.isSpawned();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.id = data.readInt();
        this.position = data.readVector2f();
        this.rot = data.readFloat();
        this.shipClassName = data.readStringFromBuffer(256);
        this.isSpawned = data.readBoolean();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeVector2f(position);
        data.writeFloat(rot);
        data.writeStringToBuffer(shipClassName);
        data.writeBoolean(isSpawned);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.getCore().getWorld();
        if (world.getEntityById(id) == null) {
            try {
                Class<?> clazz = Class.forName(shipClassName);
                Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, float.class, float.class);
                Ship ship = (Ship) ctr.newInstance(world, id, position.x, position.y, rot);
                ship.init();

                if (isSpawned) ship.setSpawmed();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                log.error("Couldn't create ship instance", e);
            }
        }
    }
}