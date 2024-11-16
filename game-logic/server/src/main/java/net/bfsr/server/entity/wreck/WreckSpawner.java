package net.bfsr.server.entity.wreck;

import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

import java.util.Random;

public final class WreckSpawner {
    private static final Random RAND = ServerGameLogic.getInstance().getWorld().getRand();
    private static final Vector2f ANGLE_TO_VELOCITY = new Vector2f();
    private static final WreckRegistry WRECK_REGISTRY = ServerGameLogic.getInstance().getConfigConverterManager()
            .getConverter(WreckRegistry.class);

    public static void spawnDestroyShipSmall(Ship ship) {
        float x = ship.getX();
        float y = ship.getY();
        Vector2 linearVelocity = ship.getLinearVelocity();
        World w = ship.getWorld();
        Random rand = w.getRand();
        spawnDamageDebris(w, rand.nextInt(3), x, y, linearVelocity.x * 0.025f, linearVelocity.y * 0.025f, 1.0f);
        spawnDamageWrecks(w, rand.nextInt(2), x, y, linearVelocity.x * 0.25f, linearVelocity.y * 0.25f);
    }

    public static void spawnDamageDebris(World world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI,
                    4.0f + RAND.nextFloat() * 2.0f, ANGLE_TO_VELOCITY);
            ANGLE_TO_VELOCITY.add(velocityX, velocityY).mul(0.7f);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size2 = (1.0F - RAND.nextFloat() / 3.0F) * 2.0f * size;
            int maxLifeTime = world.convertToTicks(30);
            boolean isFire = RAND.nextInt(3) == 0;
            boolean isFireExplosion = isFire && RAND.nextInt(5) == 0;
            int wreckIndex = RAND.nextInt(6);
            world.add(world.getObjectPools().getWrecksPool().get().init(world, world.getNextId(), wreckIndex, false, isFire,
                    isFireExplosion, x, y, ANGLE_TO_VELOCITY.x, ANGLE_TO_VELOCITY.y, LUT.sin(angle), LUT.cos(angle), angleVel,
                    size2, size2, maxLifeTime, WreckType.SMALL, WRECK_REGISTRY.getWreck(WreckType.SMALL, wreckIndex)));
        }
    }

    private static void spawnDamageWrecks(World world, int count, float x, float y, float velocityX, float velocityY) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI, 4.0f + RAND.nextFloat() * 2.0f,
                    ANGLE_TO_VELOCITY);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size = (1.0F - RAND.nextFloat() / 3.0F) * 4.0f;
            int maxLifeTime = world.convertToTicks(60);
            boolean isFireExplosion = RAND.nextInt(4) == 0;
            int wreckIndex = RAND.nextInt(3);
            world.add(world.getObjectPools().getWrecksPool().get().init(world, world.getNextId(), wreckIndex, true, true,
                    isFireExplosion, x, y, ANGLE_TO_VELOCITY.x + velocityX * 0.7f, ANGLE_TO_VELOCITY.y + velocityY * 0.7f,
                    LUT.sin(angle), LUT.cos(angle), angleVel, size, size, maxLifeTime, WreckType.DEFAULT,
                    WRECK_REGISTRY.getWreck(WreckType.DEFAULT, wreckIndex)));
        }
    }
}