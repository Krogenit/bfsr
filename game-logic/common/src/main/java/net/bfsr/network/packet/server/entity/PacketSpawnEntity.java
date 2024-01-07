package net.bfsr.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.common.PacketScheduled;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnType;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSpawnEntity extends PacketScheduled {
    private static final EntityPacketSpawnType[] PACKET_SPAWN_TYPES = EntityPacketSpawnType.values();

    private EntityPacketSpawnData entityPacketSpawnData;

    public PacketSpawnEntity(EntityPacketSpawnData entityPacketSpawnData, double timestamp) {
        super(timestamp);
        this.entityPacketSpawnData = entityPacketSpawnData;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeShort(entityPacketSpawnData.getType().ordinal());
        entityPacketSpawnData.writeData(data);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        entityPacketSpawnData = PACKET_SPAWN_TYPES[data.readShort()].createSpawnData();
        entityPacketSpawnData.readData(data);
    }
}