package net.bfsr.client.world.entity;

import lombok.AllArgsConstructor;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;

@AllArgsConstructor
public enum EntitySpawnLogicType {
    RIGID_BODY(new RigidBodySpawnLogic()),
    SHIP(new ShipSpawnLogic()),
    SHIP_WRECK(new ShipWreckSpawnLogic()),
    WRECK(new WreckSpawnLogic()),
    BULLET(new BulletSpawnLogic());

    private final EntitySpawnLogic spawnLogic;

    public void spawn(EntityPacketSpawnData spawnData) {
        spawnLogic.spawn(spawnData);
    }
}