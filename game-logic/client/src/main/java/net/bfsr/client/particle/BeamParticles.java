package net.bfsr.client.particle;

import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.engine.Engine;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static net.bfsr.client.particle.ParticleManager.PARTICLE_POOL;

public class BeamParticles {
    private final WeaponSlotBeam slot;
    private final Vector4f color;
    private final UnorderedArrayList<Particle> particles = new UnorderedArrayList<>();
    private final UnorderedArrayList<Particle> lightingParticles = new UnorderedArrayList<>();
    private final Vector2f angleToVelocity = new Vector2f();
    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();

    public BeamParticles(WeaponSlotBeam slot, Vector4f color) {
        this.slot = slot;
        this.color = color;
    }

    public void onShot() {
        weaponSpawnAccumulator.resetTime();

        Vector2f size = slot.getSize();
        Ship ship = slot.getShip();
        Vector2f position = slot.getPosition();
        particles.add(PARTICLE_POOL.get().init(Engine.assetsManager.getTexture(TextureRegister.particleLight).getTextureHandle(),
                position.x, position.y, 0, 0, 0, 0, slot.getSin(), slot.getCos(), 0.0f, size.x * 2.5f, size.y * 2.5f, 0,
                color.x, color.y, color.z, color.w, 0, false, RenderLayer.DEFAULT_ADDITIVE, particle -> {
                    particle.setSin(ship.getSin());
                    particle.setCos(ship.getCos());
                    particle.setPosition(position.x, position.y);
                    particle.getRender().getColor().w = slot.getBeamPower() * 0.6f;
                }));

        spawnBeam(1.0f, 0.3333f);
        spawnBeam(0.3333f, 1.0f);
    }

    private void spawnBeam(float sizeYMultiplayer, float colorMultiplayer) {
        Vector2f slotScale = slot.getSize();
        Vector2f slotPos = slot.getPosition();
        Ship ship = slot.getShip();
        float cos = ship.getCos();
        float sin = ship.getSin();

        float startRange = -slotScale.x;

        float startX = cos * startRange;
        float startY = sin * startRange;

        float localX = startX + cos * slot.getCurrentBeamRange();
        float localY = startY + sin * slot.getCurrentBeamRange();

        float worldX = slotPos.x + localX * 0.5f;
        float worldY = slotPos.y + localY * 0.5f;

        particles.add(PARTICLE_POOL.get().init(Engine.assetsManager.getTexture(TextureRegister.particleBeam).getTextureHandle(),
                worldX, worldY, 0, 0, 0, 0, sin, cos, 0.0f, 0.0f, slotScale.y * sizeYMultiplayer, 0,
                color.x, color.y, color.z, color.w * colorMultiplayer, 0, false, RenderLayer.DEFAULT_ADDITIVE, particle -> {
                    float cos1 = ship.getCos();
                    float sin1 = ship.getSin();

                    particle.setSin(sin1);
                    particle.setCos(cos1);

                    float startRange1 = -slotScale.x;

                    float startX1 = cos1 * startRange1;
                    float startY1 = sin1 * startRange1;

                    float localX1 = startX1 + cos1 * slot.getCurrentBeamRange();
                    float localY1 = startY1 + sin1 * slot.getCurrentBeamRange();

                    particle.setPosition(slotPos.x + localX1 * 0.5f, slotPos.y + localY1 * 0.5f);

                    Vector2f particleSize = particle.getSize();
                    particle.getRender().getLastSize().set(particleSize);
                    particleSize.x = (float) Math.sqrt(
                            (localX1 - startX1) * (localX1 - startX1) + (localY1 - startY1) * (localY1 - startY1));
                    particleSize.y = slotScale.y * sizeYMultiplayer;

                    Vector4f particleColor = particle.getRender().getColor();
                    particle.getRender().getLastColor().w = particleColor.w;
                    particleColor.w = slot.getBeamPower() * colorMultiplayer;
                }));
    }

    public void update() {
        Ship ship = slot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.angleToVelocity(sin, cos, 15.0f, angleToVelocity);
        Vector2f position = slot.getPosition();
        BeamEffects.beam(position.x, position.y, 0, 0, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y, color.x,
                color.y, color.z, color.w * 0.6f, weaponSpawnAccumulator, particle -> {
                    Vector2f localPosition = particle.getLocalPosition();
                    Vector2f velocity = particle.getVelocity();
                    localPosition.add(velocity);
                    velocity.x *= 0.99f;
                    velocity.y *= 0.99f;

                    particle.getSize().add(particle.getSizeVelocity(), particle.getSizeVelocity());
                    particle.setSin(ship.getSin());
                    particle.setCos(ship.getCos());
                    particle.setPosition(position.x + localPosition.x, position.y + localPosition.y);
                });

        while (lightingParticles.size() < slot.getCurrentBeamRange() / 10.0f) {
            lightingParticles.add(BeamEffects.beamEffect(slot, color));
        }

        for (int i = 0; i < lightingParticles.size(); i++) {
            Particle p = lightingParticles.get(i);
            if (p.isDead()) {
                lightingParticles.remove(i--);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).setDead();
        }

        for (int i = 0; i < lightingParticles.size(); i++) {
            lightingParticles.get(i).setDead();
        }

        particles.clear();
        lightingParticles.clear();
    }
}