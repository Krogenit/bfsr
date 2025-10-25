package net.bfsr.client.world.entity;

import net.bfsr.client.damage.DamageHandler;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.network.packet.common.entity.spawn.DamageableRigidBodySpawnData;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.ConnectedObjectSpawnData;

import java.util.List;

abstract class DamageableRigidBodySpawnLogic<T extends DamageableRigidBodySpawnData> implements EntitySpawnLogic<T> {
    void updateDamage(DamageHandler damageHandler, DamageableRigidBody rigidBody, T spawnData) {
        damageHandler.updateDamage(rigidBody, 0, 0, rigidBody.getDamageMask().getWidth(), rigidBody.getDamageMask().getHeight(),
                spawnData.getDamageMaskByteBuffer());
        Engine.getRenderer().memFree(spawnData.getDamageMaskByteBuffer());
    }

    void addFixturesAndConnectedObjects(DamageableRigidBody rigidBody, T spawnData, ConfigConverterManager configConverterManager) {

        List<ConnectedObjectSpawnData> connectedObjectSpawnData = spawnData.getConnectedObjectSpawnData();
        for (int i = 0; i < connectedObjectSpawnData.size(); i++) {
            ConnectedObjectSpawnData coSpawnData = connectedObjectSpawnData.get(i);

            ConnectedObject<?> connectedObject = coSpawnData.create(
                    configConverterManager.getConverter(coSpawnData.getConfigConvertedId()).get(coSpawnData.getConfigId()));

            rigidBody.initConnectedObject(connectedObject);
            rigidBody.addConnectedObject(connectedObject);
        }

        rigidBody.setFixtures(spawnData.getFixtures());
    }
}