package net.bfsr.engine.network.packet.common.world;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.engine.network.util.ByteBufUtils;
import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.WORLD_SNAPSHOT)
public class PacketWorldSnapshot extends PacketScheduled {
    private double time;
    private UnorderedArrayList<EntityData> entityDataList;

    public PacketWorldSnapshot(UnorderedArrayList<EntityData> entityDataList, int frame, double time) {
        super(frame);
        this.time = time;
        this.entityDataList = new UnorderedArrayList<>(entityDataList);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeDouble(time);
        data.writeShort(entityDataList.size());

        for (int i = 0; i < entityDataList.size(); i++) {
            EntityData entityData = entityDataList.get(i);
            entityData.write(data);
        }
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        time = data.readDouble();
        int size = data.readShort();
        entityDataList = new UnorderedArrayList<>(size);
        for (int i = 0; i < size; i++) {
            EntityData entityData = new EntityData();
            entityData.read(data);
            entityDataList.add(entityData);
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public static class EntityData {
        private int entityId;
        private float x, y;
        private float sin;
        private float cos;
        private Vector2f velocity;
        private float angularVelocity;

        public EntityData(RigidBody rigidBody) {
            entityId = rigidBody.getId();
            x = rigidBody.getX();
            y = rigidBody.getY();
            sin = rigidBody.getSin();
            cos = rigidBody.getCos();
            Vector2 linearVelocity = rigidBody.getLinearVelocity();
            velocity = new Vector2f(linearVelocity.x, linearVelocity.y);
            angularVelocity = rigidBody.getAngularVelocity();
        }

        public void write(ByteBuf data) {
            data.writeInt(entityId);
            data.writeFloat(x);
            data.writeFloat(y);
            data.writeFloat(sin);
            data.writeFloat(cos);
            ByteBufUtils.writeVector(data, velocity);
            data.writeFloat(angularVelocity);
        }

        public void read(ByteBuf data) {
            entityId = data.readInt();
            x = data.readFloat();
            y = data.readFloat();
            sin = data.readFloat();
            cos = data.readFloat();
            ByteBufUtils.readVector(data, velocity = new Vector2f());
            angularVelocity = data.readFloat();
        }
    }
}