package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.ShipWreck;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

public class PacketSpawnWreck implements PacketIn {
    protected int id, destroyedShipId;
    protected int wreckIndex;
    protected boolean isFire, isLight, isFireExplosion;
    protected float rot, alphaVelocity, rotationSpeed, wreckLifeTime, wreckMaxLifeTime;
    protected Vector2f pos, velocity, size;
    protected WreckType wreckType;
    private final WreckType[] values = WreckType.values();

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();

        wreckIndex = data.readInt();
        wreckType = values[data.readByte()];

        if (wreckType == WreckType.SHIP) {
            wreckLifeTime = data.readFloat();
            wreckMaxLifeTime = data.readFloat();
            destroyedShipId = data.readInt();
        } else {
            isFire = data.readBoolean();
            isLight = data.readBoolean();
            isFireExplosion = data.readBoolean();
            alphaVelocity = data.readFloat();
        }

        ByteBufUtils.readVector(data, pos = new Vector2f());
        ByteBufUtils.readVector(data, velocity = new Vector2f());
        rot = data.readFloat();
        rotationSpeed = data.readFloat();
        ByteBufUtils.readVector(data, size = new Vector2f());
    }

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            if (wreckType == WreckType.SHIP) {
                GameObject obj = world.getEntityById(destroyedShipId);
                if (obj instanceof Ship ship) {
                    ParticleSpawner.PARTICLE_SHIP_WREAK_POOL.getOrCreate(ShipWreck::new).init(id, wreckIndex, ship, pos.x, pos.y, velocity.x, velocity.y,
                            rot, rotationSpeed, size.x, size.y, 0.5f, 0.5f, 0.5f, 1.0f, wreckMaxLifeTime);
                }
            } else {
                ParticleSpawner.PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(wreckIndex, isLight, isFire, isFireExplosion, pos.x, pos.y, velocity.x, velocity.y,
                        rot, rotationSpeed, size.x, size.y, 0.5f, 0.5f, 0.5f, 1.0f, alphaVelocity, id, wreckType);
            }
        }
    }
}
