package net.bfsr.server.world;

import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityIdManager;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.physics.collision.CollisionMatrix;
import net.bfsr.server.dedicated.DedicatedServerGameLogic;
import net.bfsr.server.engine.EmptyGameLoop;
import net.bfsr.server.entity.EntityManager;
import net.bfsr.server.physics.CollisionHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ShipOutfitterTest {
    private static DedicatedServerGameLogic gameLogic;
    private static World world;

    @BeforeAll
    static void setup() {
        System.setProperty("assets.path", "../../");
        Profiler profiler = new Profiler();
        EventBus eventBus = new EventBus();
        gameLogic = new DedicatedServerGameLogic(new EmptyGameLoop(), profiler, eventBus);
        EntityManager entityManager = new EntityManager();
        EntityIdManager entityIdManager = new EntityIdManager();
        CollisionMatrix collisionMatrix = new CollisionMatrix(new CollisionHandler(gameLogic, gameLogic.getEventBus(),
                gameLogic.getDamageSystem(), gameLogic.getEntityTrackingManager(), gameLogic.getWreckSpawner()));
        world = new World(gameLogic.getProfiler(), 0, gameLogic.getEventBus(), entityManager, entityIdManager, gameLogic,
                collisionMatrix);
    }

    @Test
    void addGunsTest() {
        ShipFactory shipFactory = gameLogic.getShipFactory();
        Ship ship = shipFactory.createBotHumanSmall(world, 0, 0, 0.0f, Ai.NO_AI);

        Modules modules = ship.getModules();
        List<WeaponSlot> weaponSlots = modules.getWeaponSlots();
        List<Module> modulesByType = modules.getModulesByType(ModuleType.WEAPON_SLOT);

        Assertions.assertThat(weaponSlots).isNotNull();
        Assertions.assertThat(weaponSlots.size()).isGreaterThan(0);
        Assertions.assertThat(modulesByType).isNotNull();
        Assertions.assertThat(modulesByType.size()).isGreaterThan(0);
        Assertions.assertThat(weaponSlots.size()).isEqualTo(modulesByType.size());

        int weaponSlotsCount = weaponSlots.size();

        shipFactory.getShipOutfitter().addBeamGuns(ship);

        Assertions.assertThat(weaponSlots.size()).isEqualTo(weaponSlotsCount);
        Assertions.assertThat(modulesByType.size()).isEqualTo(weaponSlotsCount);
    }
}
