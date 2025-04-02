package net.bfsr.engine.network.packet.common.world.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.world.entity.RigidBody;

import java.io.IOException;

@NoArgsConstructor
@Getter
@PacketAnnotation(id = CommonPacketRegistry.ENTITY_SPAWN)
public class PacketEntitySpawn extends PacketScheduled {
    @SuppressWarnings("rawtypes")
    private EntityPacketSpawnData entityPacketSpawnData;

    public PacketEntitySpawn(RigidBody rigidBody, double timestamp) {
        super(timestamp);
        this.entityPacketSpawnData = rigidBody.createSpawnData();
        //noinspection unchecked
        this.entityPacketSpawnData.setData(rigidBody);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeByte(entityPacketSpawnData.getTypeId());
        entityPacketSpawnData.writeData(data);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        entityPacketSpawnData = gameLogic.getEntitySpawnData(data.readByte());
        entityPacketSpawnData.readData(data);
    }
}