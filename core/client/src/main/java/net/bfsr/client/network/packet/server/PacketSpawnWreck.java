package net.bfsr.client.network.packet.server;

import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.entity.wreck.ShipWreck;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.PacketBuffer;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;

public class PacketSpawnWreck implements PacketIn {
    protected int id, destroyedShipId;
    protected int wreckIndex;
    protected boolean isFire, isLight, isFireExplosion;
    protected float rot, alphaVelocity, rotationSpeed, wreckLifeTime, wreckMaxLifeTime;
    protected Vector2f pos, velocity, size;
    protected Vector4f color;
    protected WreckType wreckType;
    private final WreckType[] values = WreckType.values();

    @Override
    public void read(PacketBuffer data) throws IOException {
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

        pos = data.readVector2f();
        velocity = data.readVector2f();
        rot = data.readFloat();
        rotationSpeed = data.readFloat();
        size = data.readVector2f();
        color = data.readVector4f();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            if (wreckType == WreckType.SHIP) {
                CollisionObject obj = world.getEntityById(destroyedShipId);
                if (obj instanceof Ship ship) {
                    ParticleSpawner.PARTICLE_SHIP_WREAK_POOL.getOrCreate(ShipWreck::new).init(id, wreckIndex, ship, pos.x, pos.y, velocity.x, velocity.y,
                            rot, rotationSpeed, size.x, size.y, color.x, color.y, color.z, color.w, wreckMaxLifeTime);
                }
            } else {
                ParticleSpawner.PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(wreckIndex, isLight, isFire, isFireExplosion, pos.x, pos.y, velocity.x, velocity.y,
                        rot, rotationSpeed, size.x, size.y, color.x, color.y, color.z, color.w, alphaVelocity, id, wreckType);
            }
        }
    }
}
