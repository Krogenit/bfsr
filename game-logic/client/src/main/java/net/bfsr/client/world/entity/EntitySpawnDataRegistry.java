package net.bfsr.client.world.entity;

import net.bfsr.client.damage.DamageHandler;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.entity.EntityTypes;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnType;

import java.util.function.Supplier;

public class EntitySpawnDataRegistry {
    @SuppressWarnings("rawtypes")
    private final EntitySpawnLogic[] spawnLogics = new EntitySpawnLogic[EntityTypes.values().length];
    @SuppressWarnings("unchecked")
    private final Supplier<EntityPacketSpawnData>[] spawnDataArray = new Supplier[EntityTypes.values().length];
    private final ConfigConverterManager configConverterManager;
    private final AbstractRenderer renderer = Engine.getRenderer();

    public EntitySpawnDataRegistry(ConfigConverterManager configConverterManager, ShipFactory shipFactory, DamageHandler damageHandler,
                                   GameLogic gameLogic) {
        this.configConverterManager = configConverterManager;
        registerSpawnData();
        registerSpawnLogics(shipFactory, damageHandler, gameLogic);
    }

    private void registerSpawnData() {
        EntityPacketSpawnType[] spawnTypes = EntityPacketSpawnType.VALUES;
        for (int i = 0; i < spawnTypes.length; i++) {
            spawnDataArray[i] = spawnTypes[i].get();
        }
    }

    private void registerSpawnLogics(ShipFactory shipFactory, DamageHandler damageHandler, GameLogic gameLogic) {
        spawnLogics[EntityTypes.RIGID_BODY.ordinal()] = new RigidBodySpawnLogic();
        spawnLogics[EntityTypes.SHIP.ordinal()] = new ShipSpawnLogic(shipFactory, damageHandler);
        spawnLogics[EntityTypes.SHIP_WRECK.ordinal()] = new ShipWreckSpawnLogic(configConverterManager.getConverter(ShipRegistry.class),
                damageHandler);
        spawnLogics[EntityTypes.WRECK.ordinal()] = new WreckSpawnLogic(configConverterManager.getConverter(WreckRegistry.class),
                gameLogic.addObjectPool(Wreck.class, new ObjectPool<>(Wreck::new)));
        spawnLogics[EntityTypes.BULLET.ordinal()] = new BulletSpawnLogic(configConverterManager.getConverter(GunRegistry.class));
    }

    public EntityPacketSpawnData createSpawnData(int entityId) {
        return spawnDataArray[entityId].get();
    }

    public void spawn(int entityTypeId, EntityPacketSpawnData spawnData, World world) {
        // noinspection unchecked
        spawnLogics[entityTypeId].spawn(spawnData, world, configConverterManager, renderer);
    }
}
