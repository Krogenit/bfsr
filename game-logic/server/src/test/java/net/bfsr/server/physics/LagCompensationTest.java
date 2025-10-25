package net.bfsr.server.physics;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.Engine;
import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.network.LagCompensation;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.TransformData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.dedicated.DedicatedServer;
import net.bfsr.server.dedicated.DedicatedServerGameLogic;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class LagCompensationTest {
    private static DedicatedServer dedicatedServer;

    @BeforeAll
    static void setup() {
        System.setProperty("assets.path", "../../");
        dedicatedServer = new DedicatedServer(DedicatedServerGameLogic.class);
        dedicatedServer.getGameLogic().init();
    }

    @Test
    void positionRestoringAfterCompensationTest() {
        ServerGameLogic gameLogic = dedicatedServer.getGameLogic();
        ShipFactory shipFactory = gameLogic.getShipFactory();
        World world = gameLogic.getWorld();
        Ship shootingShip = createShipWithBeamGunsAndLagCompensatingRayCastManager();
        world.add(shootingShip);

        List<Ship> ships = new ArrayList<>(3);
        ships.add(shipFactory.createBotSaimonSmall(world, 0.5f, 0.5f, 0.0f, Ai.NO_AI));
        ships.add(shipFactory.createBotSaimonSmall(world, 0.0f, 0.5f, 0.0f, Ai.NO_AI));
        ships.add(shipFactory.createBotSaimonSmall(world, 0.5f, 0.0f, 0.0f, Ai.NO_AI));

        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            world.add(ship);
            ship.setSpawned();
        }

        Assertions.assertThat(ships.get(0).getX()).isEqualTo(0.5f);
        Assertions.assertThat(ships.get(0).getY()).isEqualTo(0.5f);
        Assertions.assertThat(ships.get(1).getX()).isEqualTo(0.0f);
        Assertions.assertThat(ships.get(1).getY()).isEqualTo(0.5f);
        Assertions.assertThat(ships.get(2).getX()).isEqualTo(0.5f);
        Assertions.assertThat(ships.get(2).getY()).isEqualTo(0.0f);

        int steps = 3;
        for (int i = 0; i < steps; i++) {
            /*
             * Ship update logic restrict liner velocity
             */
            for (int j = 0; j < ships.size(); j++) {
                Ship ship = ships.get(j);
                ship.getBody().setLinearVelocity(1.0f * Engine.UPDATES_PER_SECOND, 1.0f * Engine.UPDATES_PER_SECOND);
            }

            int frame = dedicatedServer.getFrame();
            double time = dedicatedServer.getTime();
            dedicatedServer.update(frame, time);
            dedicatedServer.setFrame(frame + 1);
            dedicatedServer.setTime(time + Engine.getTimeBetweenUpdatesInNanos());
        }

        /*
         * Test ship position after world steps
         */
        Ship testShip = ships.get(0);
        float x = testShip.getX();
        float y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.5f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.5f, Offset.offset(0.1f));

        testShip = ships.get(1);
        x = testShip.getX();
        y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.0f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.5f, Offset.offset(0.1f));

        testShip = ships.get(2);
        x = testShip.getX();
        y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.5f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.0f, Offset.offset(0.1f));

        LagCompensationRayCastManager rayCastManager = (LagCompensationRayCastManager) shootingShip.getRayCastManager();
        rayCastManager.setCompensateTimeInFrames(steps);

        /*
         * Test entity history positions
         */
        EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

        for (int i = 0; i < steps; i++) {
            TransformData transformData = dataHistoryManager.getTransformData(ships.get(0).getId(), i);
            Assertions.assertThat(transformData).isNotNull();
            Assertions.assertThat(transformData.getPosition().x).isCloseTo(0.5f + 1.0f * i, Offset.offset(0.1f));
        }

        /*
         * Ray cast with lag compensation
         */
        shootingShip.shoot(weaponSlot -> {});
        shootingShip.update();
        shootingShip.postPhysicsUpdate();

        /*
         * Test ship position after lag compensation
         */
        testShip = ships.get(0);
        x = testShip.getX();
        y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.5f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.5f, Offset.offset(0.1f));

        testShip = ships.get(1);
        x = testShip.getX();
        y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.0f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.5f, Offset.offset(0.1f));

        testShip = ships.get(2);
        x = testShip.getX();
        y = testShip.getY();

        Assertions.assertThat(x).isCloseTo(3.5f, Offset.offset(0.1f));
        Assertions.assertThat(y).isCloseTo(3.0f, Offset.offset(0.1f));
    }

    private Ship createShipWithBeamGunsAndLagCompensatingRayCastManager() {
        ServerGameLogic gameLogic = dedicatedServer.getGameLogic();
        ShipFactory shipFactory = gameLogic.getShipFactory();
        World world = gameLogic.getWorld();
        Ship ship = shipFactory.createBotHumanSmall(world, -0.5f, -0.5f, 0.0f, Ai.NO_AI);
        ship.setRayCastManager(new LagCompensationRayCastManager(world, new LagCompensation()));
        ship.setSpawned();
        shipFactory.getShipOutfitter().addBeamGuns(ship);

        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        Assertions.assertThat(weaponSlots.size()).isGreaterThan(0);

        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            Assertions.assertThat(weaponSlot).isInstanceOf(WeaponSlotBeam.class);
        }

        return ship;
    }
}
