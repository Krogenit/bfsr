package net.bfsr.client.network.packet.server.entity.wreck;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.particle.effect.ParticleSpawner;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

public class PacketSpawnWreck implements PacketIn {
    protected int id;
    protected int wreckIndex;
    protected boolean isFire, isLight, isFireExplosion;
    protected float rot, alphaVelocity, rotationSpeed;
    protected Vector2f pos, velocity, size;
    protected WreckType wreckType;
    private final WreckType[] values = WreckType.values();

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();

        wreckIndex = data.readInt();
        wreckType = values[data.readByte()];

        isFire = data.readBoolean();
        isLight = data.readBoolean();
        isFireExplosion = data.readBoolean();
        alphaVelocity = data.readFloat();

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
            ParticleSpawner.PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(wreckIndex, isLight, isFire, isFireExplosion, pos.x, pos.y, velocity.x, velocity.y,
                    rot, rotationSpeed, size.x, size.y, 0.5f, 0.5f, 0.5f, 1.0f, alphaVelocity, id, wreckType);
        }
    }
}