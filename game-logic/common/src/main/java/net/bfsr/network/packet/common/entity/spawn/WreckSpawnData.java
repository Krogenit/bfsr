package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

@Getter
@NoArgsConstructor
public class WreckSpawnData extends RigidBodySpawnData {
    private static final WreckType[] WRECK_TYPES = WreckType.values();

    private int wreckIndex;
    private int maxLifeTime;
    private boolean isFire, isLight, isFireExplosion;
    private float rotationSpeed;
    private Vector2f velocity, size;
    private WreckType wreckType;

    public WreckSpawnData(Wreck wreck) {
        super(wreck);
        this.wreckIndex = wreck.getWreckIndex();
        this.isFire = wreck.isFire();
        this.isLight = wreck.isLight();
        this.isFireExplosion = wreck.isEmitFire();
        this.maxLifeTime = wreck.getMaxLifeTime();
        this.rotationSpeed = wreck.getAngularVelocity();
        this.velocity = wreck.getVelocity();
        this.size = wreck.getSize();
        this.wreckType = wreck.getWreckType();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(wreckIndex);
        data.writeByte(wreckType.ordinal());

        data.writeBoolean(isFire);
        data.writeBoolean(isLight);
        data.writeBoolean(isFireExplosion);
        data.writeInt(maxLifeTime);

        ByteBufUtils.writeVector(data, velocity);
        data.writeFloat(rotationSpeed);
        ByteBufUtils.writeVector(data, size);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        wreckIndex = data.readInt();
        wreckType = WRECK_TYPES[data.readByte()];

        isFire = data.readBoolean();
        isLight = data.readBoolean();
        isFireExplosion = data.readBoolean();
        maxLifeTime = data.readInt();

        ByteBufUtils.readVector(data, velocity = new Vector2f());
        rotationSpeed = data.readFloat();
        ByteBufUtils.readVector(data, size = new Vector2f());
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.WRECK;
    }
}