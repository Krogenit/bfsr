package net.bfsr.client.network.packet.server.entity.wreck;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.io.IOException;

public class PacketSpawnWreck implements PacketIn {
    private int id;
    private int wreckIndex;
    private boolean isFire, isLight, isFireExplosion;
    private float sin, cos;
    private float alphaVelocity, rotationSpeed;
    private Vector2f pos, velocity, size;
    private WreckType wreckType;
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
        sin = data.readFloat();
        cos = data.readFloat();
        rotationSpeed = data.readFloat();
        ByteBufUtils.readVector(data, size = new Vector2f());
    }

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            world.addWreck(World.WREAK_POOL.getOrCreate(Wreck::new).init(world, id, wreckIndex, isLight, isFire, isFireExplosion, pos.x, pos.y,
                    velocity.x, velocity.y, sin, cos, rotationSpeed, size.x, size.y, alphaVelocity, wreckType));
        }
    }
}