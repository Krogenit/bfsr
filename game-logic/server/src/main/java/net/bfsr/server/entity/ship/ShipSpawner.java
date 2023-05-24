package net.bfsr.server.entity.ship;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class ShipSpawner {
    private final World world;
    private float timer;

    private void spawnShips() {
        if (timer-- > 0) return;

        boolean sameFaction = true;
        int botCount = 0;
        Faction lastFaction = null;
        List<Ship> ships = world.getShips();
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
            timer = 10;
            int maxCount = 1;
            int count = maxCount;

            float rotation = MathUtils.TWO_PI / 3;
            Vector2f pos = RotationHelper.angleToVelocity(0, 200);
            float spawnRandomOffset = 75;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.addShip(ShipFactory.get().createBotHumanSmall(world, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }

            RotationHelper.rotate(rotation, pos.x, pos.y, pos);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.addShip(ShipFactory.get().createBotSaimonSmall(world, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }

            RotationHelper.rotate(rotation, pos.x, pos.y, pos);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                world.addShip(ShipFactory.get().createBotEngiSmall(world, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }
        }
    }

    public void update() {
        spawnShips();
    }
}