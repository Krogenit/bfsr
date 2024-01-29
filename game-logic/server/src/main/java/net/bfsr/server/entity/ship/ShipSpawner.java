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

    public void update(World world) {
        spawnShips(world);
    }
}