package net.bfsr.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.engine.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnType;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSpawnEntity extends PacketScheduled {
    private EntityPacketSpawnData entityPacketSpawnData;

    public PacketSpawnEntity(EntityPacketSpawnData entityPacketSpawnData, double timestamp) {
        super(timestamp);
        this.entityPacketSpawnData = entityPacketSpawnData;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeByte(entityPacketSpawnData.getTypeId());
        entityPacketSpawnData.writeData(data);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        entityPacketSpawnData = EntityPacketSpawnType.get(data.readByte()).createSpawnData();
        entityPacketSpawnData.readData(data);
    }
}