package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.Client;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.network.packet.common.entity.spawn.ship.EngineSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ship.ShipSpawnData;
import net.bfsr.world.World;

import java.util.EnumMap;
import java.util.List;

@RequiredArgsConstructor
public class ShipSpawnLogic extends DamageableRigidBodySpawnLogic<ShipSpawnData> {
    private final ShipFactory shipFactory;

    @Override
    public void spawn(ShipSpawnData spawnData) {
        World world = Client.get().getWorld();

        Ship ship = shipFactory.create(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(),
                Faction.get(spawnData.getFaction()), shipFactory.getShipRegistry().get(spawnData.getDataId()),
                new DamageMask(32, 32, null));
        ship.setName(spawnData.getName());
        ship.setPolygon(spawnData.getPolygon());

        ship.init(world, spawnData.getEntityId());
        outfit(ship, spawnData, shipFactory);

        //Should be called before adding to world for proper ShipRender with connected objects initialization
        addFixturesAndConnectedObjects(ship, spawnData);

        world.add(ship, false);

        //This method should be called after ship added to world
        if (spawnData.isSpawned()) ship.setSpawned();

        //Render instance with mask texture only available after ShipRender created when ship has added to world
        updateDamage(ship, spawnData);
    }

    private void outfit(Ship ship, ShipSpawnData shipSpawnData, ShipFactory shipFactory) {
        EnumMap<Direction, List<EngineSpawnData>> enginesMap = shipSpawnData.getEnginesMap();

        shipFactory.getShipOutfitter().outfit(ship, shipSpawnData.getReactorDataId(), shipSpawnData.getEnginesDataId(),
                shipSpawnData.getHullDataId(), shipSpawnData.getArmorDataId(), shipSpawnData.getCrewDataId(),
                shipSpawnData.getCargoDataId(), shipSpawnData.getShieldDataId());

        Engines engines = ship.getModules().getEngines();
        enginesMap.forEach((direction, engineSpawnDataList) -> {
            List<Engine> engineList = engines.getEngines(direction);
            for (int i = 0; i < engineList.size(); i++) {
                EngineSpawnData engineSpawnData = engineSpawnDataList.get(i);
                Engine engine = engineList.get(i);
                engine.setId(engineSpawnData.id());
                if (engineSpawnData.isDead()) {
                    engine.setDead();
                }
            }
        });

        if (shipSpawnData.isHasShield()) {
            Shield shield = ship.getModules().getShield();

            if (shipSpawnData.isShieldDead()) {
                shield.setDead();
            } else {
                shield.setShieldHp(shipSpawnData.getShieldHp());
                shield.setRebuildingTime(shipSpawnData.getShieldRebuildingTime());
            }
        }
    }
}