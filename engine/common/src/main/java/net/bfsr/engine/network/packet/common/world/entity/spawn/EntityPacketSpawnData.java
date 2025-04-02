package net.bfsr.engine.network.packet.common.world.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.engine.world.entity.RigidBody;

@Getter
public abstract class EntityPacketSpawnData<T extends RigidBody> {
    protected int entityId;
    protected float posX, posY;
    protected float sin, cos;

    public void setData(T rigidBody) {
        this.entityId = rigidBody.getId();
        this.posX = rigidBody.getX();
        this.posY = rigidBody.getY();
        this.sin = rigidBody.getSin();
        this.cos = rigidBody.getCos();
    }

    public void readData(ByteBuf data) {
        entityId = data.readInt();
        posX = data.readFloat();
        posY = data.readFloat();
        sin = data.readFloat();
        cos = data.readFloat();
    }

    public void writeData(ByteBuf data) {
        data.writeInt(entityId);
        data.writeFloat(posX);
        data.writeFloat(posY);
        data.writeFloat(sin);
        data.writeFloat(cos);
    }

    public abstract int getTypeId();
}