package net.bfsr.client.particle.effect;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.entity.Particle;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class WeaponEffects {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2f cachedVector = new Vector2f();
    private final ParticleEffect smallWeapon;
    private final ParticleEffect bulletHit;
    private final ParticleEffect lightingIon;

    WeaponEffects(ParticleEffectsRegistry effectsRegistry) {
        smallWeapon = effectsRegistry.get("weapon/small");
        bulletHit = effectsRegistry.get("weapon/bullet_hit");
        lightingIon = effectsRegistry.get("weapon/lighting_ion");
    }

    public void spawnWeaponShoot(float worldX, float worldY, float localX, float localY, float sin, float cos, float size,
                                 float r, float g, float b, float a, Consumer<Particle> updateLogic) {
        smallWeapon.play(worldX, worldY, localX, localY, size, size, sin, cos, 0, 0, r, g, b, a, updateLogic);
    }

    public void spawnDirectedSpark(float contactX, float contactY, float normalX, float normalY, float size, float r,
                                   float g, float b, float a) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        bulletHit.playSinCos(contactX, contactY, size, LUT.sin(angle), LUT.cos(angle), r, g, b, a);
    }

    public void lightingIon(Vector2f pos, float size) {
        int count = random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), size / 4.0f, cachedVector);
            lightingIon.play(pos.x + cachedVector.x, pos.y + cachedVector.y, size, size);
        }
    }
}