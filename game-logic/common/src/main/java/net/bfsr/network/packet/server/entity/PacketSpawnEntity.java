package net.bfsr.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnType;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSpawnEntity extends PacketAdapter {
    private static final EntityPacketSpawnType[] PACKET_SPAWN_TYPES = EntityPacketSpawnType.values();

    private EntityPacketSpawnData entityPacketSpawnData;

    public PacketSpawnEntity(EntityPacketSpawnData entityPacketSpawnData) {
        this.entityPacketSpawnData = entityPacketSpawnData;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeShort(entityPacketSpawnData.getType().ordinal());
        entityPacketSpawnData.writeData(data);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        entityPacketSpawnData = PACKET_SPAWN_TYPES[data.readShort()].createSpawnData();
        entityPacketSpawnData.readData(data);
    }
}