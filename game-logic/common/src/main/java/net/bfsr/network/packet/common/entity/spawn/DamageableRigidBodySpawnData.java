package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.damage.*;
import net.bfsr.engine.Engine;
import net.bfsr.network.util.ByteBufUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.locationtech.jts.geom.Polygon;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public abstract class DamageableRigidBodySpawnData<T extends DamageableRigidBody<?>> extends RigidBodySpawnData {
    protected Polygon polygon;
    protected List<BodyFixture> fixtures;

    protected int maskWidth, maskHeight;
    private byte[] damageMaskBytes;
    private ByteBuffer damageMaskByteBuffer;

    protected List<ConnectedObject<?>> connectedObjects;

    private T rigidBody;

    DamageableRigidBodySpawnData(DamageableRigidBody<?> damageableRigidBody) {
        super(damageableRigidBody);
        this.polygon = (Polygon) damageableRigidBody.getPolygon().copy();
        DamageMask damageMask = damageableRigidBody.getMask();
        this.maskWidth = damageMask.getWidth();
        this.maskHeight = damageMask.getHeight();
        this.damageMaskBytes = damageMask.copy();

        List<ConnectedObject<?>> connectedObjects = damageableRigidBody.getConnectedObjects();
        this.connectedObjects = new ArrayList<>(connectedObjects.size());
        for (int i = 0; i < connectedObjects.size(); i++) {
            this.connectedObjects.add(connectedObjects.get(i));
        }
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        ByteBufUtils.writePolygon(data, polygon);
        data.writeShort(maskWidth);
        data.writeShort(maskHeight);
        data.writeBytes(damageMaskBytes);

        data.writeShort(connectedObjects.size());
        for (int i = 0; i < connectedObjects.size(); i++) {
            ConnectedObject<?> connectedObject = connectedObjects.get(i);
            data.writeByte(connectedObject.getConnectedObjectType().ordinal());
            data.writeInt(connectedObject.getRegistryId());
            data.writeInt(connectedObject.getDataId());
            connectedObject.writeData(data);
        }
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        polygon = ByteBufUtils.readPolygon(data);
        maskWidth = data.readShort();
        maskHeight = data.readShort();
        damageMaskByteBuffer = Engine.renderer.createByteBuffer(maskWidth * maskHeight);
        data.readBytes(damageMaskByteBuffer);
        damageMaskByteBuffer.position(0);

        fixtures = new ArrayList<>(32);
        DamageSystem.decompose(polygon, convex -> fixtures.add(new BodyFixture(convex)));

        rigidBody = createRigidBody();

        short connectedObjectsCount = data.readShort();
        connectedObjects = new ArrayList<>(connectedObjectsCount);
        for (int i = 0; i < connectedObjectsCount; i++) {
            ConnectedObjectType type = ConnectedObjectType.get(data.readByte());
            GameObjectConfigData configData = ((GameObjectConfigData) ConfigConverterManager.INSTANCE.getConverter(data.readInt())
                    .get(data.readInt()));
            ConnectedObject<?> connectedObject = type.createInstance(configData);
            connectedObject.readData(data);
            connectedObjects.add(connectedObject);
        }
    }

    protected abstract T createRigidBody();
}