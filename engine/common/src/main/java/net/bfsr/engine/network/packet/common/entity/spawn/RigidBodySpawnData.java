package net.bfsr.engine.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.entity.RigidBody;

@NoArgsConstructor
@Getter
public class RigidBodySpawnData extends EntityPacketSpawnData {
    protected int registryId;
    protected int dataId;

    public RigidBodySpawnData(RigidBody rigidBody) {
        super(rigidBody);
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

    @Override
    public int getTypeId() {
        return 0;
    }
}