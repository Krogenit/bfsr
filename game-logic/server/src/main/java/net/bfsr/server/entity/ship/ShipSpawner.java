package net.bfsr.server.entity.ship;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.server.ai.AiFactory;
import org.joml.Vector2f;

import java.util.List;

@RequiredArgsConstructor
public class ShipSpawner {
    private final XoRoShiRo128PlusRandom rand = new XoRoShiRo128PlusRandom();
    private float timer;
    private final Vector2f angleToVelocity = new Vector2f();
    private final ShipFactory shipFactory;
    private final AiFactory aiFactory;

    private Ship hugeShip;

    private void spawnShips(World world) {
        if (timer-- > 0) {
            return;
        }

        boolean sameFaction = true;
        int botCount = 0;
        Faction lastFaction = null;
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship s = ships.get(i);
            if (s.isBot()) botCount++;

            if (lastFaction != null && lastFaction != s.getFaction()) {
                sameFaction = false;
            }

            lastFaction = s.getFaction();
        }

        if (botCount < 100 || sameFaction) {
            timer = 40;
            int maxCount = 1;
            int count = maxCount;

            float rotation = MathUtils.TWO_PI / 3;
            RotationHelper.angleToVelocity(0, 30, angleToVelocity);
            float spawnRandomOffset = 7.5f;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(shipFactory.createBotHumanSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, aiFactory.createAi()), false);
            }

            RotationHelper.rotate(rotation, angleToVelocity.x, angleToVelocity.y, angleToVelocity);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(shipFactory.createBotSaimonSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, aiFactory.createAi()), false);
            }

            RotationHelper.rotate(rotation, angleToVelocity.x, angleToVelocity.y, angleToVelocity);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(shipFactory.createBotEngiSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, aiFactory.createAi()), false);
            }
        }

        if (hugeShip == null || hugeShip.isDead()) {
            world.add(hugeShip = shipFactory.createBot(world, -30, -20, rand.nextFloat() * MathUtils.TWO_PI, Faction.ENGI,
                    shipFactory.getShipRegistry().get("engi_huge0"), aiFactory.createAi()));
        }
    }

    private void spawnShipsSides(World world) {
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        boolean sameFaction = true;
        if (ships.size() != 0) {
            Faction faction = ships.get(0).getFaction();
            for (int i = 1; i < ships.size(); i++) {
                Ship ship = ships.get(i);
                if (ship.getFaction() != faction) {
                    sameFaction = false;
                    break;
                }
            }
        }

        if (ships.size() > 1 && !sameFaction)
            return;

        int count = 100;

        float padding = 10;
        float startSpawnX = -50;
        float startSpawnY = -50;
        Faction firstFaction = Faction.get((byte) rand.nextInt(3));
        for (int i = 0; i < count; i++) {
            if (firstFaction == Faction.HUMAN) {
                world.add(shipFactory.createBotHumanSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            } else if (firstFaction == Faction.SAIMON) {
                world.add(shipFactory.createBotSaimonSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            } else {
                world.add(shipFactory.createBotEngiSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            }
            startSpawnY += padding;
        }

        startSpawnX = 50;
        startSpawnY = -50;
        Faction secondFaction = Faction.get((byte) rand.nextInt(3));
        while (secondFaction == firstFaction) {
            secondFaction = Faction.get((byte) rand.nextInt(3));
        }

        for (int i = 0; i < count; i++) {
            if (secondFaction == Faction.HUMAN) {
                world.add(shipFactory.createBotHumanSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            } else if (secondFaction == Faction.SAIMON) {
                world.add(shipFactory.createBotSaimonSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            } else {
                world.add(shipFactory.createBotEngiSmall(world, startSpawnX, startSpawnY, 0, aiFactory.createAi()), false);
            }

            startSpawnY += padding;
        }
    }

    public void update(World world) {
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            if (Math.abs(ship.getX()) >= 100 || Math.abs(ship.getY()) >= 100) {
                ship.setDead();
            }
        }

        spawnShips(world);
    }
}