package net.bfsr.server.entity.ship;

import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.List;
import java.util.Random;

public class ShipSpawner {
    private float timer;
    private final Vector2f angleToVelocity = new Vector2f();

    private void spawnShips(World world) {
        if (timer-- > 0) return;

        boolean sameFaction = true;
        int botCount = 0;
        Faction lastFaction = null;
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        Random rand = world.getRand();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship s = ships.get(i);
            if (s.isBot()) botCount++;

            if (lastFaction != null && lastFaction != s.getFaction()) {
                sameFaction = false;
            }

            lastFaction = s.getFaction();
        }

        if (botCount < 50 || sameFaction) {
            timer = 40;
            int maxCount = 1;
            int count = maxCount;

            float rotation = MathUtils.TWO_PI / 3;
            RotationHelper.angleToVelocity(0, 200, angleToVelocity);
            float spawnRandomOffset = 75;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(ShipFactory.get().createBotHumanSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, AiFactory.createAi()), false);
            }

            RotationHelper.rotate(rotation, angleToVelocity.x, angleToVelocity.y, angleToVelocity);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(ShipFactory.get().createBotSaimonSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, AiFactory.createAi()), false);
            }

            RotationHelper.rotate(rotation, angleToVelocity.x, angleToVelocity.y, angleToVelocity);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.add(ShipFactory.get().createBotEngiSmall(world, angleToVelocity.x + addX, angleToVelocity.y + addY,
                        rand.nextFloat() * MathUtils.TWO_PI, AiFactory.createAi()), false);
            }
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
        float startSpawnX = -500;
        float startSpawnY = -500;
        Random rand = world.getRand();
        Faction firstFaction = Faction.get((byte) rand.nextInt(3));
        for (int i = 0; i < count; i++) {
            if (firstFaction == Faction.HUMAN)
                world.add(ShipFactory.get().createBotHumanSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()), false);
            else if (firstFaction == Faction.SAIMON)
                world.add(ShipFactory.get().createBotSaimonSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()),
                        false);
            else
                world.add(ShipFactory.get().createBotEngiSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()), false);
            startSpawnY += padding;
        }

        startSpawnX = 500;
        startSpawnY = -500;
        Faction secondFaction = Faction.get((byte) rand.nextInt(3));
        while (secondFaction == firstFaction) {
            secondFaction = Faction.get((byte) rand.nextInt(3));
        }

        for (int i = 0; i < count; i++) {
            if (secondFaction == Faction.HUMAN)
                world.add(ShipFactory.get().createBotHumanSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()), false);
            else if (secondFaction == Faction.SAIMON)
                world.add(ShipFactory.get().createBotSaimonSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()),
                        false);
            else
                world.add(ShipFactory.get().createBotEngiSmall(world, startSpawnX, startSpawnY, 0, AiFactory.createAi()), false);

            startSpawnY += padding;
        }
    }

    public void update(World world) {
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            Vector2f position = ship.getPosition();
            if (Math.abs(position.x) >= 1000 || Math.abs(position.y) >= 1000) {
                ship.setDead();
            }
        }

        spawnShips(world);
    }
}