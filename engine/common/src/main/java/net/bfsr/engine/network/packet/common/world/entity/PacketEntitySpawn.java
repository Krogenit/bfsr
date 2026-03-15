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
    private byte entityType;
    @SuppressWarnings("rawtypes")
    private EntityPacketSpawnData entityPacketSpawnData;

    public PacketEntitySpawn(RigidBody rigidBody, int frame) {
        super(frame);
        this.entityType = (byte) rigidBody.getEntityType();
        this.entityPacketSpawnData = rigidBody.createSpawnData();
        //noinspection unchecked
        this.entityPacketSpawnData.setData(rigidBody);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeByte(entityType);
        entityPacketSpawnData.writeData(data);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        entityType = data.readByte();
        entityPacketSpawnData = gameLogic.getEntitySpawnData(entityType);
        entityPacketSpawnData.readData(data);
    }
}