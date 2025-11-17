package net.bfsr.server.entity.wreck;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.RequiredArgsConstructor;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

@RequiredArgsConstructor
public final class WreckSpawner {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2f angleToVelocity = new Vector2f();
    private final WreckRegistry wreckRegistry;
    private final ObjectPool<Wreck> wreckPool;

    public void onSmallShipDestroy(Ship ship) {
        float x = ship.getX();
        float y = ship.getY();
        Vector2 linearVelocity = ship.getLinearVelocity();
        World w = ship.getWorld();
        spawnSmallWrecks(w, 2 + random.nextInt(2), x, y, linearVelocity.x * 0.025f, linearVelocity.y * 0.025f, 1.0f, ship.getId());
    }

    public void spawnSmallWrecks(World world, int count, float x, float y, float velocityX, float velocityY, float size, int shipId) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(random.nextFloat() * MathUtils.TWO_PI, RandomHelper.randomFloat(random, 0.4f, 0.6f),
                    angleToVelocity);
            angleToVelocity.add(velocityX, velocityY).mul(0.7f);
            float angle = random.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + random.nextFloat() / 200.0f) * 60.0f;
            float size2 = RandomHelper.randomFloat(random, 0.132f, 0.2f) * size;
            int maxLifeTime = Engine.convertIntSecondsToFrames(30);
            boolean emitFire = random.nextInt(5) == 0;
            int wreckIndex = random.nextInt(6);
            world.add(wreckPool.get().init(world, world.getNextId(), x, y, LUT.sin(angle), LUT.cos(angle), size2, size2,
                    angleToVelocity.x, angleToVelocity.y, angleVel, maxLifeTime, shipId, emitFire, wreckRegistry.get(wreckIndex)));
        }
    }
}