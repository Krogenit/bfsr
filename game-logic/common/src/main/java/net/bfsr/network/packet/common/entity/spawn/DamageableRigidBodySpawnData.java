package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.ConnectedObjectType;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.geometry.GeometryUtils;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.engine.network.util.ByteBufUtils;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.ConnectedObjectSpawnData;
import org.jbox2d.dynamics.Fixture;
import org.locationtech.jts.geom.Polygon;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class DamageableRigidBodySpawnData<T extends DamageableRigidBody> extends RigidBodySpawnData<T> {
    protected Polygon polygon;
    protected List<Fixture> fixtures;

    private byte[] damageMaskBytes;
    private ByteBuffer damageMaskByteBuffer;

    protected List<ConnectedObject<?>> connectedObjects;
    private List<ConnectedObjectSpawnData> connectedObjectSpawnData;

    @Override
    public void setData(T damageableRigidBody) {
        super.setData(damageableRigidBody);
        this.polygon = (Polygon) damageableRigidBody.getPolygon().copy();
        this.damageMaskBytes = damageableRigidBody.getDamageMask().copy();

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
        data.writeInt(damageMaskBytes.length);
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
        int maskDataLength = data.readInt();
        damageMaskByteBuffer = Engine.getRenderer().createByteBuffer(maskDataLength);
        data.readBytes(damageMaskByteBuffer);
        damageMaskByteBuffer.position(0);

        fixtures = new ArrayList<>(32);
        GeometryUtils.decompose(polygon, convex -> fixtures.add(new Fixture(convex)));

        short connectedObjectsCount = data.readShort();
        connectedObjectSpawnData = new ArrayList<>(connectedObjectsCount);
        for (int i = 0; i < connectedObjectsCount; i++) {
            ConnectedObjectType type = ConnectedObjectType.get(data.readByte());
            ConnectedObjectSpawnData connectedObject = type.createInstance();
            connectedObject.readData(data);
            connectedObjectSpawnData.add(connectedObject);
        }
    }
}