package net.bfsr.client.world.entity;

import net.bfsr.client.Client;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.network.packet.common.entity.spawn.DamageableRigidBodySpawnData;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.ConnectedObjectSpawnData;

import java.util.List;

abstract class DamageableRigidBodySpawnLogic<T extends DamageableRigidBodySpawnData> implements EntitySpawnLogic<T> {
    private final DamageHandler damageHandler = Client.get().getDamageHandler();

    void updateDamage(DamageableRigidBody rigidBody, T spawnData) {
        damageHandler.updateDamage(rigidBody, 0, 0, rigidBody.getMask().getWidth(), rigidBody.getMask().getHeight(),
                spawnData.getDamageMaskByteBuffer());
    }

    void addFixturesAndConnectedObjects(DamageableRigidBody rigidBody, T spawnData) {
        ConfigConverterManager configConverterManager = Client.get().getConfigConverterManager();

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