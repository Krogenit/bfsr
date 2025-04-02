package net.bfsr.client.renderer.component;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.particle.BeamParticles;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.SpawnAccumulator;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import org.joml.Vector4f;

public class WeaponSlotBeamRender extends WeaponSlotRender {
    private final WeaponSlotBeam weaponSlotBeam;
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();
    private final BeamParticles beamParticles;
    @Getter
    private final Vector4f effectsColor = new Vector4f();
    private final BeamEffects beamEffects = Client.get().getParticleEffects().getBeamEffects();
    private final GarbageSpawner garbageSpawner = Client.get().getParticleEffects().getGarbageSpawner();

    private Runnable particlesUpdateRunnable = RunnableUtils.EMPTY_RUNNABLE;

    WeaponSlotBeamRender(WeaponSlotBeam weaponSlotBeam) {
        super(weaponSlotBeam);
        this.weaponSlotBeam = weaponSlotBeam;
        this.effectsColor.set(weaponSlotBeam.getGunData().getColor());
        this.effectsColor.w = 0.0f;
        this.beamParticles = new BeamParticles(weaponSlotBeam, effectsColor);
        weaponSlotBeam.getWeaponSlotEventBus().register(this);
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();
        effectsColor.w = weaponSlotBeam.getBeamPower();
        particlesUpdateRunnable.run();
    }

    @Override
    public void onShot() {
        damageSpawnAccumulator.resetTime();
        playSounds(weaponSlotBeam.getGunData(), object.getX(), object.getY());
        beamParticles.onShot();
        particlesUpdateRunnable = () -> {
            beamParticles.update();

            if (weaponSlotBeam.getBeamPower() <= 0.0f) {
                particlesUpdateRunnable = RunnableUtils.EMPTY_RUNNABLE;
                beamParticles.clear();
            }
        };
    }

    private void onDamage(float normalX, float normalY, float hitX, float hitY) {
        beamEffects.beamDamage(hitX, hitY, normalX, normalY, object.getSizeX(), effectsColor, damageSpawnAccumulator);
    }

    @EventHandler
    public EventListener<BeamDamageShipShieldEvent> beamDamageShipShieldEvent() {
        return event -> onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());
    }

    @EventHandler
    public EventListener<BeamDamageShipArmorEvent> beamDamageShipArmorEvent() {
        return event -> {
            Ship ship = event.ship();
            onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());

            garbageSpawner.beamArmorDamage(event.hitX(), event.hitY(), ship.getLinearVelocity().x * 0.005f,
                    ship.getLinearVelocity().y * 0.005f);
        };
    }

    @EventHandler
    public EventListener<BeamDamageHullEvent> beamDamageShipHullEvent() {
        return event -> {
            RigidBody rigidBody = event.rigidBody();
            onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());

            garbageSpawner.beamHullDamage(event.hitX(), event.hitY(), rigidBody.getLinearVelocity().x * 0.005f,
                    rigidBody.getLinearVelocity().y * 0.005f);
        };
    }

    @EventHandler
    public EventListener<BeamDamageWreckEvent> beamDamageWreckEvent() {
        return event -> {
            Wreck wreck = event.wreck();
            onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());
            garbageSpawner.beamHullDamage(event.hitX(), event.hitY(), wreck.getLinearVelocity().x * 0.005f,
                    wreck.getLinearVelocity().y * 0.005f);
        };
    }

    @Override
    public void clear() {
        super.clear();
        weaponSlotBeam.getWeaponSlotEventBus().unregister(this);
        beamParticles.clear();
    }
}