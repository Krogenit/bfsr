package net.bfsr.client.world.entity;

import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;

import java.util.EnumMap;

public class EntitySpawnLoginRegistry {
    @SuppressWarnings("rawtypes")
    private final EnumMap<EntitySpawnLogicType, EntitySpawnLogic> map = new EnumMap<>(EntitySpawnLogicType.class);

    public void init(ConfigConverterManager configConverterManager) {
        ShipRegistry shipRegistry = configConverterManager.getConverter(ShipRegistry.class);

        map.put(EntitySpawnLogicType.RIGID_BODY, new RigidBodySpawnLogic());
        map.put(EntitySpawnLogicType.SHIP, new ShipSpawnLogic(new ShipFactory(shipRegistry, new ShipOutfitter(configConverterManager))));
        map.put(EntitySpawnLogicType.SHIP_WRECK, new ShipWreckSpawnLogic(shipRegistry));
        map.put(EntitySpawnLogicType.WRECK, new WreckSpawnLogic(configConverterManager.getConverter(WreckRegistry.class)));
        map.put(EntitySpawnLogicType.BULLET, new BulletSpawnLogic(configConverterManager.getConverter(GunRegistry.class)));
    }

    public void spawn(EntitySpawnLogicType spawnLogicType, EntityPacketSpawnData spawnData) {
        //noinspection unchecked
        map.get(spawnLogicType).spawn(spawnData);
    }
}
