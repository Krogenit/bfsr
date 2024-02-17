package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.common.entity.spawn.ShipSpawnData;
import net.bfsr.world.World;

public class ShipSpawnLogic extends DamageableRigidBodySpawnLogic<ShipSpawnData> {
    @Override
    public void spawn(ShipSpawnData spawnData) {
        World world = Core.get().getWorld();
        Ship ship = spawnData.getRigidBody();
        ship.init(world, spawnData.getEntityId());
        spawnData.outfit(ship);

        //Should be called before adding to world for proper ShipRender with connected objects initialization
        addFixturesAndConnectedObjects(ship, spawnData);

        world.add(ship, false);

        //This method should be called after ship added to world
        if (spawnData.isSpawned()) ship.setSpawned();

        //Render instance with mask texture only available after ShipRender created when ship has added to world
        updateDamage(ship, spawnData);
    }
}