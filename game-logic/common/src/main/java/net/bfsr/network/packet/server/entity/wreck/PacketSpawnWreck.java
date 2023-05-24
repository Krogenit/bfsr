package net.bfsr.network.packet.server.entity.wreck;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSpawnWreck extends PacketAdapter {
    private int id;
    private int wreckIndex;
    private boolean isFire, isLight, isFireExplosion;
    private float sin, cos;
    private float alphaVelocity, rotationSpeed;
    private Vector2f pos, velocity, size;
    private WreckType wreckType;
    private final WreckType[] values = WreckType.values();

    public PacketSpawnWreck(Wreck p) {
        this.id = p.getId();
        this.wreckIndex = p.getWreckIndex();
        this.isFire = p.isFire();
        this.isLight = p.isLight();
        this.isFireExplosion = p.isEmitFire();
        this.alphaVelocity = p.getLifeTimeVelocity();
        this.rotationSpeed = p.getAngularVelocity();
        this.velocity = p.getVelocity();
        this.size = p.getSize();
        this.pos = p.getPosition();
        this.sin = p.getSin();
        this.cos = p.getCos();
        this.wreckType = p.getWreckType();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);

        data.writeInt(wreckIndex);
        data.writeByte(wreckType.ordinal());

        data.writeBoolean(isFire);
        data.writeBoolean(isLight);
        data.writeBoolean(isFireExplosion);
        data.writeFloat(alphaVelocity);

        ByteBufUtils.writeVector(data, pos);
        ByteBufUtils.writeVector(data, velocity);
        data.writeFloat(sin);
        data.writeFloat(cos);
        data.writeFloat(rotationSpeed);
        ByteBufUtils.writeVector(data, size);
    }

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
}