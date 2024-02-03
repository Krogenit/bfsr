package net.bfsr.client.renderer.component;

import lombok.Getter;
import net.bfsr.client.particle.BeamParticles;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponSlotBeamRender extends WeaponSlotRender<WeaponSlotBeam> {
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();
    private final BeamParticles beamParticles;
    @Getter
    private final Vector4f effectsColor = new Vector4f();
    private Runnable particlesUpdateRunnable = RunnableUtils.EMPTY_RUNNABLE;

    WeaponSlotBeamRender(WeaponSlotBeam object) {
        super(object);
        this.effectsColor.set(object.getGunData().getColor());
        this.effectsColor.w = 0.0f;
        this.beamParticles = new BeamParticles(object, effectsColor);
        object.getWeaponSlotEventBus().register(this);
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();
        effectsColor.w = object.getBeamPower();
        particlesUpdateRunnable.run();
    }

    @Override
    public void onShot() {
        damageSpawnAccumulator.resetTime();
        Vector2f position = object.getPosition();
        playSounds(object.getGunData(), object.getShip().getWorld().getRand(), position.x, position.y);
        beamParticles.onShot();
        particlesUpdateRunnable = () -> {
            beamParticles.update();

            if (object.getBeamPower() <= 0.0f) {
                particlesUpdateRunnable = RunnableUtils.EMPTY_RUNNABLE;
                beamParticles.clear();
            }
        };
    }

    private void onDamage(float normalX, float normalY, float hitX, float hitY) {
        BeamEffects.beamDamage(hitX, hitY, normalX, normalY, object.getSize().x, effectsColor, damageSpawnAccumulator);
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

            GarbageSpawner.beamArmorDamage(event.hitX(), event.hitY(), ship.getVelocity().x * 0.005f,
                    ship.getVelocity().y * 0.005f);
        };
    }

    @EventHandler
    public EventListener<BeamDamageShipHullEvent> beamDamageShipHullEvent() {
        return event -> {
            Ship ship = event.ship();
            onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());

            GarbageSpawner.beamHullDamage(event.hitX(), event.hitY(), ship.getVelocity().x * 0.005f,
                    ship.getVelocity().y * 0.005f);
        };
    }

    @EventHandler
    public EventListener<BeamDamageWreckEvent> beamDamageWreckEvent() {
        return event -> {
            Wreck wreck = event.wreck();
            onDamage(event.normalX(), event.normalY(), event.hitX(), event.hitY());
            GarbageSpawner.beamHullDamage(event.hitX(), event.hitY(), wreck.getVelocity().x * 0.005f,
                    wreck.getVelocity().y * 0.005f);
        };
    }

    @Override
    public void clear() {
        object.getWeaponSlotEventBus().unregister(this);
        beamParticles.clear();
    }
}