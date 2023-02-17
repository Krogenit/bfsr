package net.bfsr.server.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.wreck.Wreck;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketSpawnWreck implements PacketOut {
    protected int id, destroyedShipId;
    protected int wreckIndex;
    protected boolean isFire, isLight, isFireExplosion;
    protected float rot, alphaVelocity, rotationSpeed, wreckLifeTime, wreckMaxLifeTime;
    protected Vector2f pos, velocity, size;
    protected WreckType wreckType;

    public PacketSpawnWreck(Wreck p) {
        this.id = p.getId();
        this.destroyedShipId = p.getDestroyedShipId();
        this.wreckIndex = p.getWreckIndex();
        this.isFire = p.isFire();
        this.isLight = p.isLight();
        this.isFireExplosion = p.isFireExplosion();
        this.alphaVelocity = p.getLifeTimeVelocity();
        this.rotationSpeed = p.getAngularVelocity();
        this.velocity = p.getVelocity();
        this.size = p.getScale();
        this.pos = p.getPosition();
        this.rot = p.getRotation();
        this.wreckType = p.getWreckType();
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
    }
}