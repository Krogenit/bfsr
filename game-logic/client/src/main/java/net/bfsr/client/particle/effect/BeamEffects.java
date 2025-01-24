package net.bfsr.client.particle.effect;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.Particle;
import net.bfsr.engine.entity.ParticleManager;
import net.bfsr.engine.entity.SpawnAccumulator;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.particle.ParticleRender;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class BeamEffects {
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final long beamEffectTextureHandle;
    private final ParticleManager particleManager;
    private final ParticleEffect smallBeam;

    BeamEffects(ParticleManager particleManager, ParticleEffectsRegistry effectsRegistry) {
        this.particleManager = particleManager;
        this.beamEffectTextureHandle = Engine.getAssetsManager().getTexture(TextureRegister.particleBeamEffect).getTextureHandle();

        smallBeam = effectsRegistry.get("weapon/beam/small");
    }

    public void beamDamage(float x, float y, float normalX, float normalY, float size, Vector4f color,
                           SpawnAccumulator spawnAccumulator) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        beam(x, y, size, LUT.sin(angle), LUT.cos(angle), 0, 0, color.x, color.y, color.z, color.w, spawnAccumulator);
    }

    public void beam(float x, float y, float size, float sin, float cos, float velocityX, float velocityY, float r,
                     float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallBeam.emit(x, y, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public void beam(float x, float y, float localX, float localY, float size, float sin, float cos, float velocityX,
                     float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator,
                     Consumer<Particle> updateLogic, Consumer<ParticleRender> lastValuesUpdateConsumer) {
        smallBeam.emit(x, y, localX, localY, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator,
                updateLogic, lastValuesUpdateConsumer);
    }

    public Particle beamEffect(WeaponSlotBeam slot, Vector4f color) {
        Ship ship = slot.getShip();
        float localX = random.nextFloat();
        float localY = (random.nextFloat() * 2.0f - 1.0f) * slot.getSizeY() / 2.0f;

        float beamRange = slot.getCurrentBeamRange();

        float cos = ship.getCos();
        float sin = ship.getSin();

        float l = beamRange * localX + (random.nextFloat() * 2.0f - 1.0f);

        float worldX = cos * l - sin * localY + slot.getX();
        float worldY = sin * l + cos * localY + slot.getY();

        return particleManager.createParticle().init(beamEffectTextureHandle, worldX, worldY, localX, localY, 0.0f, 0.0f, sin, cos, 0.0f,
                5.0f + 2.8f * random.nextFloat(), slot.getSizeY() / 2.0f + 0.4f * random.nextFloat(), 0.0f, color.x, color.y,
                color.z, color.w, 0.5f, false, RenderLayer.DEFAULT_ADDITIVE, particle -> {
                    float sin1 = ship.getSin();
                    float cos1 = ship.getCos();
                    float beamRange1 = slot.getCurrentBeamRange();

                    float offsetX = beamRange1 * localX + (random.nextFloat() * 2.0f - 1.0f);

                    particle.setRotation(sin1, cos1);
                    particle.setPosition(cos1 * offsetX - sin1 * localY + slot.getX(), sin1 * offsetX + cos1 * localY + slot.getY());
                }, particleRender -> {
                    particleRender.setLastPosition();
                    particleRender.setLastRotation();
                });
    }
}