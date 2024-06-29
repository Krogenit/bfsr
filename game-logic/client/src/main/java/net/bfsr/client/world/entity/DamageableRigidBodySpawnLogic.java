package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.network.packet.common.entity.spawn.DamageableRigidBodySpawnData;
import org.dyn4j.geometry.MassType;

import java.util.List;

abstract class DamageableRigidBodySpawnLogic<T extends DamageableRigidBodySpawnData<?>> implements EntitySpawnLogic<T> {
    private final DamageHandler damageHandler = Core.get().getDamageHandler();

    void updateDamage(DamageableRigidBody rigidBody, T spawnData) {
        damageHandler.updateDamage(rigidBody, 0, 0, rigidBody.getMask().getWidth(), rigidBody.getMask().getHeight(),
                spawnData.getDamageMaskByteBuffer());
    }

    void addFixturesAndConnectedObjects(DamageableRigidBody rigidBody, T spawnData) {
        List<ConnectedObject<?>> connectedObjects = spawnData.getConnectedObjects();
        for (int i = 0; i < connectedObjects.size(); i++) {
            ConnectedObject<?> connectedObject = connectedObjects.get(i);
            rigidBody.initConnectedObject(connectedObject);
            rigidBody.addConnectedObject(connectedObject);
        }

        rigidBody.setFixtures(spawnData.getFixtures());
        rigidBody.getBody().setMass(MassType.NORMAL);
    }
}