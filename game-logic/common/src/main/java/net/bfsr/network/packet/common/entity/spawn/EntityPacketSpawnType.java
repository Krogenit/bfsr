package net.bfsr.network.packet.common.entity.spawn;

import lombok.AllArgsConstructor;
import net.bfsr.network.packet.common.entity.spawn.ship.ShipSpawnData;

import java.util.function.Supplier;

@AllArgsConstructor
public enum EntityPacketSpawnType {
    RIGID_BODY(RigidBodySpawnData::new),
    SHIP(ShipSpawnData::new),
    SHIP_WRECK(ShipWreckSpawnData::new),
    WRECK(WreckSpawnData::new),
    BULLET(BulletSpawnData::new);

    private static final EntityPacketSpawnType[] VALUES = values();

    private final Supplier<EntityPacketSpawnData> supplier;

    public EntityPacketSpawnData createSpawnData() {
        return supplier.get();
    }

    public static EntityPacketSpawnType get(byte index) {
        return VALUES[index];
    }
}