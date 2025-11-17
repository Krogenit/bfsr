package net.bfsr.client.particle;

import net.bfsr.client.Client;
import net.bfsr.client.assets.TextureRegister;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.AssetsManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.renderer.particle.ParticleType;
import net.bfsr.engine.world.entity.ParticleManager;
import net.bfsr.engine.world.entity.SpawnAccumulator;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class BeamParticles {
    private final AssetsManager assetsManager = Engine.getAssetsManager();

    private final WeaponSlotBeam slot;
    private final Vector4f color;
    private final Vector2f angleToVelocity = new Vector2f();
    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final ParticleManager particleManager = Client.get().getParticleManager();
    private final BeamEffects beamEffects = Client.get().getParticleEffects().getBeamEffects();
    private final long lightTextureHandle = assetsManager.getTexture(TextureRegister.light.getTextureData()).getTextureHandle();
    private final long beamTextureHandle = assetsManager.getTexture(TextureRegister.beam.getTextureData()).getTextureHandle();

    private boolean isDead;

    public BeamParticles(WeaponSlotBeam slot, Vector4f color) {
        this.slot = slot;
        this.color = color;
    }

    public void onShot() {
        isDead = false;
        weaponSpawnAccumulator.resetTime();

        Ship ship = slot.getShip();
        Render render = Client.get().getEntityRenderer().getRender(ship.getId());
        particleManager.createParticle().init(lightTextureHandle, slot.getX(), slot.getY(), 0, 0, render.getZ(), 0, 0, slot.getSin(),
                slot.getCos(), 0.0f, slot.getSizeX() * 2.5f, slot.getSizeY() * 2.5f, 0, color.x, color.y, color.z, color.w, 0, false,
                ParticleType.ADDITIVE, particle1 -> {
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
        float slotSizeY = slot.getSizeY();
        Ship ship = slot.getShip();
        float cos = ship.getCos();
        float sin = ship.getSin();

        float startRange = 0;

        float startX = cos * startRange;
        float startY = sin * startRange;

        float localX = startX + cos * slot.getCurrentBeamRange();
        float localY = startY + sin * slot.getCurrentBeamRange();

        float worldX = slot.getX() + localX * 0.5f;
        float worldY = slot.getY() + localY * 0.5f;
        Render render = Client.get().getEntityRenderer().getRender(ship.getId());

        particleManager.createParticle().init(beamTextureHandle, worldX, worldY, 0, 0, render.getZ(), 0, 0, sin, cos, 0.0f, 0.0f,
                slotSizeY * sizeYMultiplayer, 0, color.x, color.y, color.z, color.w * colorMultiplayer, 0, false, ParticleType.ADDITIVE,
                particle1 -> {
                    float cos1 = ship.getCos();
                    float sin1 = ship.getSin();
                    particle1.setRotation(sin1, cos1);

                    float startX1 = cos1 * startRange;
                    float startY1 = sin1 * startRange;
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
        RotationHelper.angleToVelocity(sin, cos, 1.5f, angleToVelocity);
        ShipRender shipRender = Client.get().getEntityRenderer().getRender(ship.getId());
        beamEffects.beam(slot.getX(), slot.getY(), shipRender.getZ(), 0, 0, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y,
                color.x, color.y, color.z, color.w * 0.6f, weaponSpawnAccumulator, particle -> {
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
    }

    public void clear() {
        isDead = true;
    }
}