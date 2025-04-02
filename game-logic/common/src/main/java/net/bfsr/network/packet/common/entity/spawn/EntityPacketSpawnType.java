package net.bfsr.network.packet.common.entity.spawn;

import lombok.AllArgsConstructor;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.network.packet.common.entity.spawn.ship.ShipSpawnData;

import java.util.function.Supplier;

@AllArgsConstructor
public enum EntityPacketSpawnType {
    RIGID_BODY(RigidBodySpawnData::new),
    SHIP(ShipSpawnData::new),
    SHIP_WRECK(ShipWreckSpawnData::new),
    WRECK(WreckSpawnData::new),
    BULLET(BulletSpawnData::new);

    @SuppressWarnings("PublicStaticArrayField")
    public static final EntityPacketSpawnType[] VALUES = values();

    private final Supplier<EntityPacketSpawnData> supplier;

    public Supplier<EntityPacketSpawnData> get() {
        return supplier;
    }

    public static EntityPacketSpawnType get(byte index) {
        return VALUES[index];
    }
}