package net.bfsr.network.packet.common.entity.spawn;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public enum EntityPacketSpawnType {
    RIGID_BODY(RigidBodySpawnData::new),
    SHIP(ShipSpawnData::new),
    SHIP_WRECK(ShipWreckSpawnData::new),
    WRECK(WreckSpawnData::new),
    BULLET(BulletSpawnData::new);

    private final Supplier<EntityPacketSpawnData> supplier;

    public EntityPacketSpawnData createSpawnData() {
        return supplier.get();
    }
}