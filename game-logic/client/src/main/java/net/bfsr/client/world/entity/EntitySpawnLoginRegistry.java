package net.bfsr.client.world.entity;

import net.bfsr.client.damage.DamageHandler;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.wreck.Wreck;

import java.util.EnumMap;

public class EntitySpawnLoginRegistry {
    @SuppressWarnings("rawtypes")
    private final EnumMap<EntitySpawnLogicType, EntitySpawnLogic> map = new EnumMap<>(EntitySpawnLogicType.class);
    private final ConfigConverterManager configConverterManager;
    private final AbstractRenderer renderer = Engine.getRenderer();

    public EntitySpawnLoginRegistry(ConfigConverterManager configConverterManager, ShipFactory shipFactory, DamageHandler damageHandler,
                                    GameLogic gameLogic) {
        this.configConverterManager = configConverterManager;

        map.put(EntitySpawnLogicType.RIGID_BODY, new RigidBodySpawnLogic());
        map.put(EntitySpawnLogicType.SHIP, new ShipSpawnLogic(shipFactory, damageHandler));
        map.put(EntitySpawnLogicType.SHIP_WRECK, new ShipWreckSpawnLogic(configConverterManager.getConverter(ShipRegistry.class),
                damageHandler));
        map.put(EntitySpawnLogicType.WRECK, new WreckSpawnLogic(configConverterManager.getConverter(WreckRegistry.class),
                gameLogic.addObjectPool(Wreck.class, new ObjectPool<>(Wreck::new))));
        map.put(EntitySpawnLogicType.BULLET, new BulletSpawnLogic(configConverterManager.getConverter(GunRegistry.class)));
    }

    public void spawn(EntitySpawnLogicType spawnLogicType, EntityPacketSpawnData spawnData, World world) {
        // noinspection unchecked
        map.get(spawnLogicType).spawn(spawnData, world, configConverterManager, renderer);
    }
}
