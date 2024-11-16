package net.bfsr.network.packet.common.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.entity.ChronologicalEntityData;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.PacketScheduled;
import net.bfsr.network.util.ByteBufUtils;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketWorldSnapshot extends PacketScheduled {
    private UnorderedArrayList<EntityData> entityDataList;

    public PacketWorldSnapshot(UnorderedArrayList<EntityData> entityDataList, double timestamp) {
        super(timestamp);
        this.entityDataList = new UnorderedArrayList<>(entityDataList);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeShort(entityDataList.size());

        for (int i = 0; i < entityDataList.size(); i++) {
            EntityData entityData = entityDataList.get(i);
            entityData.write(data);
        }
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        int size = data.readShort();
        entityDataList = new UnorderedArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entityDataList.add(new EntityData(data));
        }
    }

    @Override
    public boolean canProcess(double time) {
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public static class EntityData extends ChronologicalEntityData {
        private final int entityId;
        private final float x, y;
        private final float sin;
        private final float cos;
        private final Vector2f velocity;
        private final float angularVelocity;

        public EntityData(RigidBody rigidBody, double time) {
            super(time);
            entityId = rigidBody.getId();
            x = rigidBody.getX();
            y = rigidBody.getY();
            sin = rigidBody.getSin();
            cos = rigidBody.getCos();
            Vector2 linearVelocity = rigidBody.getLinearVelocity();
            velocity = new Vector2f(linearVelocity.x, linearVelocity.y);
            angularVelocity = rigidBody.getAngularVelocity();
        }

        EntityData(ByteBuf data) {
            super(data.readDouble());
            entityId = data.readInt();
            x = data.readFloat();
            y = data.readFloat();
            sin = data.readFloat();
            cos = data.readFloat();
            ByteBufUtils.readVector(data, velocity = new Vector2f());
            angularVelocity = data.readFloat();
        }

        public void write(ByteBuf data) {
            data.writeDouble(time);
            data.writeInt(entityId);
            data.writeFloat(x);
            data.writeFloat(y);
            data.writeFloat(sin);
            data.writeFloat(cos);
            ByteBufUtils.writeVector(data, velocity);
            data.writeFloat(angularVelocity);
        }
    }
}