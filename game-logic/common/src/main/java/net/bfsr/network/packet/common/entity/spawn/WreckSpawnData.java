package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import org.jbox2d.common.Vector2;

@Getter
@NoArgsConstructor
public class WreckSpawnData extends RigidBodySpawnData<Wreck> {
    private static final WreckType[] WRECK_TYPES = WreckType.values();

    private int wreckIndex;
    private int maxLifeTime;
    private boolean isFire, isLight, isFireExplosion;
    private float rotationSpeed;
    private float velocityX, velocityY;
    private float sizeX, sizeY;
    private WreckType wreckType;

    @Override
    public void setData(Wreck wreck) {
        super.setData(wreck);
        this.wreckIndex = wreck.getWreckIndex();
        this.isFire = wreck.isFire();
        this.isLight = wreck.isLight();
        this.isFireExplosion = wreck.isEmitFire();
        this.maxLifeTime = wreck.getMaxLifeTime();
        this.rotationSpeed = wreck.getAngularVelocity();
        Vector2 linearVelocity = wreck.getLinearVelocity();
        this.velocityX = linearVelocity.x;
        this.velocityY = linearVelocity.y;
        this.sizeX = wreck.getSizeX();
        this.sizeY = wreck.getSizeY();
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

        data.writeFloat(velocityX);
        data.writeFloat(velocityY);
        data.writeFloat(rotationSpeed);
        data.writeFloat(sizeX);
        data.writeFloat(sizeY);
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

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        rotationSpeed = data.readFloat();
        sizeX = data.readFloat();
        sizeY = data.readFloat();
    }

    @Override
    public int getTypeId() {
        return EntityPacketSpawnType.WRECK.ordinal();
    }
}