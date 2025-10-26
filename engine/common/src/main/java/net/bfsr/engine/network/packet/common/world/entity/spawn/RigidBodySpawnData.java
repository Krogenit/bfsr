package net.bfsr.engine.network.packet.common.world.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.engine.world.entity.RigidBody;

@Getter
public class RigidBodySpawnData<T extends RigidBody> extends EntityPacketSpawnData<T> {
    protected int registryId;
    protected int dataId;

    @Override
    public void setData(T rigidBody) {
        super.setData(rigidBody);
        this.registryId = rigidBody.getConfigData().getRegistryId();
        this.dataId = rigidBody.getConfigData().getId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(registryId);
        data.writeInt(dataId);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        registryId = data.readInt();
        dataId = data.readInt();
    }
}