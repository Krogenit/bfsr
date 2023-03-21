package net.bfsr.client.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.effect.ParticleEffect;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketSpawnParticleEffect implements PacketIn {
    private ParticleEffect particleEffect;
    private int initiatorId, affectedId;
    private float contactX, contactY;
    private float normalX, normalY;

    @Override
    public void read(ByteBuf data) throws IOException {
        particleEffect = ParticleEffect.values()[data.readShort()];
        initiatorId = data.readInt();
        affectedId = data.readInt();
        contactX = data.readFloat();
        contactY = data.readFloat();
        normalX = data.readFloat();
        normalY = data.readFloat();
    }

    @Override
    public void processOnClientSide() {
        GameObject initiator = Core.get().getWorld().getEntityById(initiatorId);
        GameObject affected = Core.get().getWorld().getEntityById(affectedId);
        if (initiator != null && affected != null) {
            ParticleEffectsRegistry.INSTANCE.emit(particleEffect.ordinal(), initiator, affected, contactX, contactY, normalX, normalY);
        }
    }
}