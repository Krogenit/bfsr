package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;

public class ShipWreckSpawnLogic extends DamageableRigidBodySpawnLogic<ShipWreckSpawnData> {
    @Override
    public void spawn(ShipWreckSpawnData spawnData) {
        ShipWreck wreck = spawnData.getRigidBody();
        World world = Core.get().getWorld();
        wreck.init(world, spawnData.getEntityId());
        Body body = wreck.getBody();
        body.setLinearVelocity(spawnData.getVelocityX(), spawnData.getVelocityY());
        body.setAngularVelocity(spawnData.getAngularVelocity());

        addFixturesAndConnectedObjects(wreck, spawnData);

        world.add(wreck);

        updateDamage(wreck, spawnData);
    }
}