package net.bfsr.client.particle;

import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.engine.AssetsManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.entity.Particle;
import net.bfsr.engine.entity.ParticleManager;
import net.bfsr.engine.entity.SpawnAccumulator;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class BeamParticles {
    private final AssetsManager assetsManager = Engine.getAssetsManager();

    private final WeaponSlotBeam slot;
    private final Vector4f color;
    private final UnorderedArrayList<Particle> lightingParticles = new UnorderedArrayList<>();
    private final Vector2f angleToVelocity = new Vector2f();
    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final ParticleManager particleManager = Client.get().getParticleManager();
    private final BeamEffects beamEffects = Client.get().getParticleEffects().getBeamEffects();

    private boolean isDead;

    public BeamParticles(WeaponSlotBeam slot, Vector4f color) {
        this.slot = slot;
        this.color = color;
    }

    public void onShot() {
        isDead = false;
        weaponSpawnAccumulator.resetTime();

        Ship ship = slot.getShip();
        particleManager.createParticle().init(assetsManager.getTexture(TextureRegister.particleLight).getTextureHandle(),
                slot.getX(), slot.getY(), 0, 0, 0, 0, slot.getSin(), slot.getCos(), 0.0f, slot.getSizeX() * 2.5f, slot.getSizeY() * 2.5f, 0,
                color.x, color.y, color.z, color.w, 0, false, RenderLayer.DEFAULT_ADDITIVE, particle1 -> {
                    particle1.setPosition(slot.getX(), slot.getY());
                    particle1.setRotation(ship.getSin(), ship.getCos());
                    particle1.getRender().setColorAlpha(slot.getBeamPower() * 0.6f);

                    if (isDead) {
                        particle1.setDead();
                    }
                }, particleRender -> {
                    particleRender.setLastPosition();
                    particleRender.setLastRotation();
                    particleRender.setLastColorAlpha();
                });

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

        particleManager.createParticle().init(assetsManager.getTexture(TextureRegister.particleBeam).getTextureHandle(),
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
                    particle1.setSize(Math.sqrt((localX1 - startX1) * (localX1 - startX1) + (localY1 - startY1) * (localY1 - startY1)),
                            slotSizeY * sizeYMultiplayer);
                    particle1.getRender().setColorAlpha(slot.getBeamPower() * colorMultiplayer);

                    if (isDead) {
                        particle1.setDead();
                    }
                }, particleRender -> {
                    particleRender.setLastPosition();
                    particleRender.setLastRotation();
                    particleRender.setLastSize();
                    particleRender.setLastColorAlpha();
                });
    }

    public void update() {
        Ship ship = slot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.angleToVelocity(sin, cos, 15.0f, angleToVelocity);
        beamEffects.beam(slot.getX(), slot.getY(), 0, 0, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y, color.x,
                color.y, color.z, color.w * 0.6f, weaponSpawnAccumulator, particle -> {
                    Vector2f localPosition = particle.getLocalPosition();
                    Vector2f velocity = particle.getVelocity();
                    localPosition.add(velocity);
                    velocity.x *= 0.99f;
                    velocity.y *= 0.99f;

                    particle.addSize(particle.getSizeVelocity(), particle.getSizeVelocity());
                    particle.setRotation(ship.getSin(), ship.getCos());
                    particle.setPosition(slot.getX() + localPosition.x, slot.getY() + localPosition.y);

                    if (isDead) {
                        particle.setDead();
                    }
                }, render -> {
                    render.setLastPosition();
                    render.setLastRotation();
                    render.setLastSize();
                });

        while (lightingParticles.size() < (int) (slot.getCurrentBeamRange() / 10)) {
            lightingParticles.add(beamEffects.beamEffect(slot, color));
        }

        for (int i = 0; i < lightingParticles.size(); i++) {
            Particle p = lightingParticles.get(i);
            if (p.isDead()) {
                lightingParticles.remove(i--);
            }
        }
    }

    public void clear() {
        isDead = true;
        for (int i = 0; i < lightingParticles.size(); i++) {
            lightingParticles.get(i).setDead();
        }

        lightingParticles.clear();
    }
}