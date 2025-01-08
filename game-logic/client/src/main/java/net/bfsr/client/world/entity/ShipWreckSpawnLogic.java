package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.Client;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import net.bfsr.world.World;
import org.jbox2d.dynamics.Body;

@RequiredArgsConstructor
public class ShipWreckSpawnLogic extends DamageableRigidBodySpawnLogic<ShipWreckSpawnData> {
    private final ShipRegistry shipRegistry;

    @Override
    public void spawn(ShipWreckSpawnData spawnData) {
        ShipData shipData = shipRegistry.get(spawnData.getDataId());
        ShipWreck wreck = new ShipWreck(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(),
                shipData.getSizeX(), shipData.getSizeY(), shipData,
                new DamageMask(spawnData.getMaskWidth(), spawnData.getMaskHeight(), null), spawnData.getPolygon(),
                spawnData.getLocalOffsetX(), spawnData.getLocalOffsetY());

        World world = Client.get().getWorld();
        wreck.init(world, spawnData.getEntityId());
        Body body = wreck.getBody();
        body.setLinearVelocity(spawnData.getVelocityX(), spawnData.getVelocityY());
        body.setAngularVelocity(spawnData.getAngularVelocity());

        addFixturesAndConnectedObjects(wreck, spawnData);

        world.add(wreck);

        updateDamage(wreck, spawnData);
    }
}