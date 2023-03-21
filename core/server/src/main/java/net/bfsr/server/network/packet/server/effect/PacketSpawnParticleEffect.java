package net.bfsr.server.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.effect.ParticleEffect;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSpawnParticleEffect implements PacketOut {
    private ParticleEffect particleEffect;
    private GameObject initiator, affected;
    private float contactX, contactY;
    private float normalX, normalY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeShort(particleEffect.ordinal());
        data.writeInt(initiator.getId());
        data.writeInt(affected.getId());
        data.writeFloat(contactX);
        data.writeFloat(contactY);
        data.writeFloat(normalX);
        data.writeFloat(normalY);
    }
}