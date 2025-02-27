package net.bfsr.server.entity.wreck;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.RequiredArgsConstructor;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

@RequiredArgsConstructor
public final class WreckSpawner {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2f angleToVelocity = new Vector2f();
    private final WreckRegistry wreckRegistry;
    private final ObjectPool<Wreck> wreckPool;

    public void spawnDestroyShipSmall(Ship ship) {
        float x = ship.getX();
        float y = ship.getY();
        Vector2 linearVelocity = ship.getLinearVelocity();
        World w = ship.getWorld();
        spawnDamageDebris(w, random.nextInt(3), x, y, linearVelocity.x * 0.025f, linearVelocity.y * 0.025f, 1.0f);
        spawnDamageWrecks(w, random.nextInt(2), x, y, linearVelocity.x * 0.25f, linearVelocity.y * 0.25f);
    }

    public void spawnDamageDebris(World world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(random.nextFloat() * MathUtils.TWO_PI,
                    4.0f + random.nextFloat() * 2.0f, angleToVelocity);
            angleToVelocity.add(velocityX, velocityY).mul(0.7f);
            float angle = random.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + random.nextFloat() / 200.0f) * 60.0f;
            float size2 = (1.0F - random.nextFloat() / 3.0F) * 2.0f * size;
            int maxLifeTime = Engine.convertToTicks(30);
            boolean isFire = random.nextInt(3) == 0;
            boolean isFireExplosion = isFire && random.nextInt(5) == 0;
            int wreckIndex = random.nextInt(6);
            world.add(wreckPool.get().init(world, world.getNextId(), wreckIndex, false, isFire, isFireExplosion, x, y, angleToVelocity.x,
                    angleToVelocity.y, LUT.sin(angle), LUT.cos(angle), angleVel, size2, size2, maxLifeTime, WreckType.SMALL,
                    wreckRegistry.getWreck(WreckType.SMALL, wreckIndex)));
        }
    }

    private void spawnDamageWrecks(World world, int count, float x, float y, float velocityX, float velocityY) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(random.nextFloat() * MathUtils.TWO_PI, 4.0f + random.nextFloat() * 2.0f,
                    angleToVelocity);
            float angle = random.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + random.nextFloat() / 200.0f) * 60.0f;
            float size = (1.0F - random.nextFloat() / 3.0F) * 4.0f;
            int maxLifeTime = Engine.convertToTicks(60);
            boolean isFireExplosion = random.nextInt(4) == 0;
            int wreckIndex = random.nextInt(3);
            world.add(wreckPool.get().init(world, world.getNextId(), wreckIndex, true, true, isFireExplosion, x, y,
                    angleToVelocity.x + velocityX * 0.7f, angleToVelocity.y + velocityY * 0.7f, LUT.sin(angle), LUT.cos(angle), angleVel,
                    size, size, maxLifeTime, WreckType.DEFAULT, wreckRegistry.getWreck(WreckType.DEFAULT, wreckIndex)));
        }
    }
}