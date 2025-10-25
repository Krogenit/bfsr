package net.bfsr.client.particle.effect;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.entity.ParticleManager;
import org.joml.Vector2f;

import java.util.function.Supplier;

public class GarbageSpawner {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2f cachedVector = new Vector2f();
    private final ParticleManager particleManager;
    private final ParticleEffect ost;

    GarbageSpawner(ParticleManager particleManager, ParticleEffectsRegistry effectsRegistry) {
        this.particleManager = particleManager;
        ost = effectsRegistry.get("garbage/ost");
    }

    public void bulletArmorDamage(float x, float y, float velocityX, float velocityY, float normalX, float normalY) {
        smallGarbage(1 + random.nextInt(3), x, y, velocityX + normalX, velocityY + normalY,
                RandomHelper.randomFloat(random, 0.055f, 0.165f), 0.3f, 0.5f);
    }

    public void bulletHullDamage(float x, float y, float velocityX, float velocityY, float normalX, float normalY) {
        if (random.nextInt(2) == 0) {
            spawnShipOst(x, y, velocityX + normalX * (random.nextFloat() * 0.5f + 0.5f),
                    velocityY + normalY * (random.nextFloat() * 0.5f + 0.5f));
        }

        smallGarbage(1 + random.nextInt(3), x, y, velocityX + normalX, velocityY + normalY,
                RandomHelper.randomFloat(random, 0.055f, 0.165f), 0.3f, 0.5f);
    }

    public void beamArmorDamage(float x, float y, float velocityX, float velocityY) {
        if (random.nextInt(5) == 0) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, cachedVector);
            smallGarbage(random.nextInt(4), x, y, velocityX + cachedVector.x, velocityY + cachedVector.y,
                    RandomHelper.randomFloat(random, 0.02f, 0.2f));
        }
    }

    public void beamHullDamage(float x, float y, float velocityX, float velocityY) {
        if (random.nextInt(5) == 0) {
            if (random.nextInt(50) == 0) {
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, cachedVector);
                spawnShipOst(x, y, velocityX + cachedVector.x, velocityY + cachedVector.y);
            }

            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, cachedVector);
            smallGarbage(random.nextInt(4), x, y, velocityX + cachedVector.x, velocityY + cachedVector.y,
                    RandomHelper.randomFloat(random, 0.02f, 0.2f));
        }
    }

    public void spawnShipOst(float x, float y, float velocityX, float velocityY) {
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.02f + random.nextFloat() * 0.2f, cachedVector);
        ost.play(x, y, 0, 0, velocityX + cachedVector.x, velocityY + cachedVector.y);
    }

    public void smallGarbage(int count, float x, float y, float velocityX, float velocityY, float size, float sizeVel,
                             float alphaVel) {
        smallGarbage(count, x, y, velocityX, velocityY, () -> cachedVector.set(0), size, sizeVel, alphaVel);
    }

    public void smallGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        smallGarbage(count, x, y, velocityX, velocityY, () -> {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.02f + random.nextFloat() * 0.2f, cachedVector);
            return cachedVector;
        }, size, 0.05f, 0.12f);
    }

    public void smallGarbage(int count, float x, float y, float velocityX, float velocityY,
                             Supplier<Vector2f> localVelocitySupplier, float size, float sizeVel, float alphaVel) {
        for (int i = 0; i < count; i++) {
            Vector2f localVelocity = localVelocitySupplier.get();
            float angularVelocity = RandomHelper.randomFloat(random, -0.06f, 0.06f);
            float angle = MathUtils.TWO_PI * random.nextFloat();
            particleManager.createParticle().init(TextureRegister.particleGarbage0, x, y, velocityX + localVelocity.x,
                    velocityY + localVelocity.y, LUT.sin(angle), LUT.cos(angle), angularVelocity, size, size, sizeVel, 0.6f, 0.6f,
                    0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }
}