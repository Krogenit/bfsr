package net.bfsr.network.packet.common.entity.spawn;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import earcut4j.Earcut;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.damage.*;
import net.bfsr.engine.Engine;
import org.dyn4j.dynamics.BodyFixture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public abstract class DamageableRigidBodySpawnData<T extends DamageableRigidBody<?>> extends RigidBodySpawnData {
    protected PathsD contours;
    protected List<BodyFixture> fixtures;

    protected int maskWidth, maskHeight;
    private byte[] damageMaskBytes;
    private ByteBuffer damageMaskByteBuffer;

    protected List<ConnectedObject<?>> connectedObjects;

    private T rigidBody;

    DamageableRigidBodySpawnData(DamageableRigidBody<?> damageableRigidBody) {
        super(damageableRigidBody);

        PathsD contours = damageableRigidBody.getContours();
        this.contours = new PathsD(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            this.contours.add(contours.get(i));
        }

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

        data.writeShort(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            PathD contour = contours.get(i);
            data.writeShort(contour.size());
            for (int j = 0; j < contour.size(); j++) {
                PointD pointD = contour.get(j);
                data.writeFloat((float) pointD.x);
                data.writeFloat((float) pointD.y);
            }
        }

        data.writeShort(maskWidth);
        data.writeShort(maskHeight);
        for (int i = 0; i < maskHeight; i++) {
            data.writeBytes(damageMaskBytes, i * maskHeight, maskWidth);
        }

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

        short contoursSize = data.readShort();
        contours = new PathsD(contoursSize);
        for (int i = 0; i < contoursSize; i++) {
            short contourSize = data.readShort();
            PathD contour = new PathD(contourSize);
            contours.add(contour);

            for (int j = 0; j < contourSize; j++) {
                contour.add(new PointD(data.readFloat(), data.readFloat()));
            }
        }

        maskWidth = data.readShort();
        maskHeight = data.readShort();
        damageMaskByteBuffer = Engine.renderer.createByteBuffer(maskWidth * maskHeight);
        data.readBytes(damageMaskByteBuffer);
        damageMaskByteBuffer.position(0);

        fixtures = new ArrayList<>(32);
        DamageSystem.decompose(contours, convex -> fixtures.add(new BodyFixture(convex)), new Earcut());

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