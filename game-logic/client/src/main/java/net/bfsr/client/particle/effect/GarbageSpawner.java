package net.bfsr.client.particle.effect;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.particle.ParticleType;
import net.bfsr.engine.renderer.texture.TextureData;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.entity.ParticleManager;
import org.joml.Vector2f;

import java.util.function.Supplier;

public class GarbageSpawner {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2f cachedVector = new Vector2f();
    private final ParticleManager particleManager;
    private final long[] debrisTextures;

    GarbageSpawner(ParticleManager particleManager) {
        this.particleManager = particleManager;
        String baseDebrisPath = "texture/particle/debris/debris$.png";
        int count = 12;
        debrisTextures = new long[count];
        for (int i = 0; i < count; i++) {
            debrisTextures[i] = Engine.getAssetsManager().getTexture(new TextureData(
                    PathHelper.convertPath(baseDebrisPath.replace("$", i + "")))).getTextureHandle();
        }
    }

    public void bulletArmorDamage(float x, float y, float z, float velocityX, float velocityY, float normalX, float normalY) {
        smallGarbage(1 + random.nextInt(3), x, y, z, velocityX + normalX, velocityY + normalY, 0.5f);
    }

    public void bulletHullDamage(float x, float y, float z, float velocityX, float velocityY, float normalX, float normalY) {
        smallGarbage(1 + random.nextInt(3), x, y, z, velocityX + normalX, velocityY + normalY, 0.5f);
    }

    public void beamArmorDamage(float x, float y, float z, float velocityX, float velocityY) {
        if (random.nextInt(5) == 0) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, cachedVector);
            smallGarbage(random.nextInt(4), x, y, z, velocityX + cachedVector.x,
                    velocityY + cachedVector.y, RandomHelper.randomFloat(random, 0.02f, 0.2f));
        }
    }

    public void beamHullDamage(float x, float y, float z, float velocityX, float velocityY) {
        if (random.nextInt(5) == 0) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, cachedVector);
            smallGarbage(random.nextInt(4), x, y, z, velocityX + cachedVector.x,
                    velocityY + cachedVector.y, RandomHelper.randomFloat(random, 0.02f, 0.2f));
        }
    }

    public void smallGarbage(int count, float x, float y, float z, float velocityX, float velocityY,
                             float alphaVel) {
        smallGarbage(count, x, y, z, velocityX, velocityY, () -> cachedVector.set(0), alphaVel);
    }

    public void smallGarbage(int count, float x, float y, float z, float velocityX, float velocityY,
                             Supplier<Vector2f> localVelocitySupplier, float alphaVel) {
        for (int i = 0; i < count; i++) {
            Vector2f localVelocity = localVelocitySupplier.get();
            float angularVelocity = RandomHelper.randomFloat(random, -0.06f, 0.06f);
            float angle = MathUtils.TWO_PI * random.nextFloat();
            float size1 = RandomHelper.randomFloat(random, 0.02f, 0.05f);
            particleManager.createParticle().init(debrisTextures[random.nextInt(debrisTextures.length)], x, y, z,
                    velocityX + localVelocity.x, velocityY + localVelocity.y, LUT.sin(angle), LUT.cos(angle), angularVelocity, size1, size1,
                    0.0f, 0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, ParticleType.ALPHA_BLENDED);
        }
    }
}