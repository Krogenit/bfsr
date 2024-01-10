package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;

public class ShipWreckSpawnLogic implements EntitySpawnLogic {
    private final Core core = Core.get();
    private final DamageHandler damageHandler = core.getDamageHandler();

    @Override
    public void spawn(EntityPacketSpawnData spawnData) {
        ShipWreckSpawnData shipWreckSpawnData = (ShipWreckSpawnData) spawnData;
        ShipWreck wreck = shipWreckSpawnData.getWreck();
        if (wreck != null) {
            World world = core.getWorld();
            wreck.init(world, spawnData.getEntityId());
            Body body = wreck.getBody();
            body.setLinearVelocity(shipWreckSpawnData.getVelocityX(), shipWreckSpawnData.getVelocityY());
            body.setAngularVelocity(shipWreckSpawnData.getAngularVelocity());

            world.add(wreck);
            damageHandler.updateDamage(wreck, 0, 0, shipWreckSpawnData.getMaskWidth(), shipWreckSpawnData.getMaskHeight(),
                    shipWreckSpawnData.getByteBuffer());
        }
    }
}