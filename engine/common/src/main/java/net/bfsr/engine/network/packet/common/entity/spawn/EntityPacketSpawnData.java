package net.bfsr.engine.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.entity.RigidBody;

@Getter
@NoArgsConstructor
public abstract class EntityPacketSpawnData {
    protected int entityId;
    protected float posX, posY;
    protected float sin, cos;

    protected EntityPacketSpawnData(RigidBody rigidBody) {
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