package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.damage.SimpleConnectedObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class DamageableRigidBodySpawnData extends RigidBodySpawnData {
    protected List<ConnectedObject> connectedObjects;

    DamageableRigidBodySpawnData(DamageableRigidBody<?> damageableRigidBody) {
        super(damageableRigidBody);
        this.connectedObjects = damageableRigidBody.getConnectedObjects();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        data.writeShort(connectedObjects.size());
        for (int i = 0; i < connectedObjects.size(); i++) {
            ConnectedObject connectedObject = connectedObjects.get(i);
            connectedObject.writeData(data);
        }
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        short connectedObjectsCount = data.readShort();
        connectedObjects = new ArrayList<>(connectedObjectsCount);
        for (int i = 0; i < connectedObjectsCount; i++) {
            ConnectedObject connectedObject = new SimpleConnectedObject();
            connectedObject.readData(data);
            connectedObjects.add(connectedObject);
        }
    }
}