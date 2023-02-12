package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ShipWreck;
import net.bfsr.client.particle.Wreck;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;

@NoArgsConstructor
public class PacketSpawnParticle extends ServerPacket {
    private int id, destroyedShipId;
    private int wreckIndex;
    private boolean isFire, isLight, isFireExplosion;
    private float rot, alphaVelocity, rotationSpeed, wreckLifeTime, wreckMaxLifeTime;
    private Vector2f pos, velocity, size;
    private Vector4f color;
    private WreckType wreckType;
    private final WreckType[] values = WreckType.values();

    public PacketSpawnParticle(Wreck p) {
        this.id = p.getId();
        this.destroyedShipId = p.getDestroyedShipId();
        this.wreckIndex = p.getWreckIndex();
        this.isFire = p.isFire();
        this.isLight = p.isLight();
        this.isFireExplosion = p.isFireExplosion();
        this.alphaVelocity = p.getAlphaVelocity();
        this.rotationSpeed = p.getAngularVelocity();
        this.velocity = p.getVelocity();
        this.size = p.getScale();
        this.pos = p.getPosition();
        this.color = p.getColor();
        this.rot = p.getRotation();
        this.wreckType = p.getWreckType();
    }

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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);

        data.writeInt(wreckIndex);
        data.writeByte(wreckType.ordinal());

        if (wreckType == WreckType.SHIP) {
            data.writeFloat(wreckLifeTime);
            data.writeFloat(wreckMaxLifeTime);
            data.writeInt(destroyedShipId);
        } else {
            data.writeBoolean(isFire);
            data.writeBoolean(isLight);
            data.writeBoolean(isFireExplosion);
            data.writeFloat(alphaVelocity);
        }

        data.writeVector2f(pos);
        data.writeVector2f(velocity);
        data.writeFloat(rot);
        data.writeFloat(rotationSpeed);
        data.writeVector2f(size);
        data.writeVector4f(color);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            if (wreckType == WreckType.SHIP) {
                CollisionObject obj = world.getEntityById(destroyedShipId);
                if (obj instanceof Ship ship) {
                    ParticleSpawner.PARTICLE_SHIP_WREAK_POOL.getOrCreate(ShipWreck::new).init(wreckIndex, ship, pos.x, pos.y, velocity.x, velocity.y,
                            rot, rotationSpeed, size.x, size.y, color.x, color.y, color.z, color.w, id, wreckMaxLifeTime);
                }
            } else {
                ParticleSpawner.PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(wreckIndex, isLight, isFire, isFireExplosion, pos.x, pos.y, velocity.x, velocity.y,
                        rot, rotationSpeed, size.x, size.y, color.x, color.y, color.z, color.w, alphaVelocity, id, wreckType);
            }
        }
    }
}