package net.bfsr.entity;

import lombok.AllArgsConstructor;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ship.ShipSpawnData;

import java.util.function.Supplier;

@AllArgsConstructor
public enum EntityTypes {
    RIGID_BODY(RigidBodySpawnData::new),
    SHIP(ShipSpawnData::new),
    SHIP_WRECK(ShipWreckSpawnData::new),
    WRECK(WreckSpawnData::new),
    BULLET(BulletSpawnData::new);

    @SuppressWarnings("PublicStaticArrayField")
    public static final EntityTypes[] VALUES = values();

    private final Supplier<EntityPacketSpawnData<?>> spawnDataSupplier;

    public EntityPacketSpawnData<?> getSpawnData() {
        return spawnDataSupplier.get();
    }

    public static EntityTypes get(byte index) {
        return VALUES[index];
    }
}
