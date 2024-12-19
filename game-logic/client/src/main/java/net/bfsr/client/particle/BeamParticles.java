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

        Ship ship = slot.getShip();
        particles.add(PARTICLE_POOL.get().init(Engine.assetsManager.getTexture(TextureRegister.particleLight).getTextureHandle(),
                slot.getX(), slot.getY(), 0, 0, 0, 0, slot.getSin(), slot.getCos(), 0.0f, slot.getSizeX() * 2.5f, slot.getSizeY() * 2.5f, 0,
                color.x, color.y, color.z, color.w, 0, false, RenderLayer.DEFAULT_ADDITIVE, particle1 -> {
                    particle1.setPosition(slot.getX(), slot.getY());
                    particle1.setRotation(ship.getSin(), ship.getCos());
                    particle1.getRender().setColorAlpha(slot.getBeamPower() * 0.6f);
                }, particleRender -> {
                    particleRender.setLastPosition();
                    particleRender.setLastRotation();
                    particleRender.setLastColorAlpha();
                }));

        spawnBeam(1.0f, 0.3333f);
        spawnBeam(0.3333f, 1.0f);
    }

    private void spawnBeam(float sizeYMultiplayer, float colorMultiplayer) {
        float slotSizeX = slot.getSizeX();
        float slotSizeY = slot.getSizeY();
        Ship ship = slot.getShip();
        float cos = ship.getCos();
        float sin = ship.getSin();

        float startRange = -slotSizeX;

        float startX = cos * startRange;
        float startY = sin * startRange;

        float localX = startX + cos * slot.getCurrentBeamRange();
        float localY = startY + sin * slot.getCurrentBeamRange();

        float worldX = slot.getX() + localX * 0.5f;
        float worldY = slot.getY() + localY * 0.5f;

        particles.add(PARTICLE_POOL.get().init(Engine.assetsManager.getTexture(TextureRegister.particleBeam).getTextureHandle(),
                worldX, worldY, 0, 0, 0, 0, sin, cos, 0.0f, 0.0f, slotSizeY * sizeYMultiplayer, 0,
                color.x, color.y, color.z, color.w * colorMultiplayer, 0, false, RenderLayer.DEFAULT_ADDITIVE, particle1 -> {
                    float cos1 = ship.getCos();
                    float sin1 = ship.getSin();
                    particle1.setRotation(sin1, cos1);

                    float startRange1 = -slotSizeX;

                    float startX1 = cos1 * startRange1;
                    float startY1 = sin1 * startRange1;

                    float localX1 = startX1 + cos1 * slot.getCurrentBeamRange();
                    float localY1 = startY1 + sin1 * slot.getCurrentBeamRange();

                    particle1.setPosition(slot.getX() + localX1 * 0.5f, slot.getY() + localY1 * 0.5f);

                    particle1.setSize(
                            (float) Math.sqrt((localX1 - startX1) * (localX1 - startX1) + (localY1 - startY1) * (localY1 - startY1)),
                            slotSizeY * sizeYMultiplayer);

                    particle1.getRender().setColorAlpha(slot.getBeamPower() * colorMultiplayer);
                }, particleRender -> {
                    particleRender.setLastPosition();
                    particleRender.setLastRotation();
                    particleRender.setLastSize();
                    particleRender.setLastColorAlpha();
                }));
    }

    public void update() {
        Ship ship = slot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.angleToVelocity(sin, cos, 15.0f, angleToVelocity);
        BeamEffects.beam(slot.getX(), slot.getY(), 0, 0, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y, color.x,
                color.y, color.z, color.w * 0.6f, weaponSpawnAccumulator, particle -> {
                    Vector2f localPosition = particle.getLocalPosition();
                    Vector2f velocity = particle.getVelocity();
                    localPosition.add(velocity);
                    velocity.x *= 0.99f;
                    velocity.y *= 0.99f;

                    particle.addSize(particle.getSizeVelocity(), particle.getSizeVelocity());
                    particle.setRotation(ship.getSin(), ship.getCos());
                    particle.setPosition(slot.getX() + localPosition.x, slot.getY() + localPosition.y);
                }, render -> {
                    render.setLastPosition();
                    render.setLastRotation();
                    render.setLastSize();
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