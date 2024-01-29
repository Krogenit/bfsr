package net.bfsr.client.renderer.component;

import lombok.Getter;
import net.bfsr.client.particle.Beam;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponSlotBeamRender extends WeaponSlotRender<WeaponSlotBeam> {
    private static final AbstractTexture LIGHT_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleLight);

    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();
    private final Beam beam;
    @Getter
    private final Vector4f effectsColor = new Vector4f();
    private final Vector2f angleToVelocity = new Vector2f();

    WeaponSlotBeamRender(WeaponSlotBeam object) {
        super(object);
        this.effectsColor.set(object.getGunData().getColor());
        this.effectsColor.w = 0.0f;
        this.beam = new Beam(object, effectsColor);
        object.getWeaponSlotEventBus().register(this);
    }

    @Override
    public void update() {
        super.update();
        beam.update();

        lastColor.w = effectsColor.w;

        if (object.getBeamPower() > 0.0f) {
            float sin = object.getShip().getSin();
            float cos = object.getShip().getCos();
            RotationHelper.angleToVelocity(sin, cos, 10.0f, angleToVelocity);
            Vector2f position = object.getPosition();
            BeamEffects.beam(position.x, position.y, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y, effectsColor.x,
                    effectsColor.y, effectsColor.z, lastColor.w + (effectsColor.w - lastColor.w)
                            * Engine.renderer.getInterpolation(), weaponSpawnAccumulator);
        }

        effectsColor.w = object.getBeamPower();
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        if (effectsColor.w > 0) {
            beam.postPhysicsUpdate();
        }
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();

        if (effectsColor.w > 0) {
            Vector2f position = object.getPosition();
            float sin = object.getShip().getSin();
            float cos = object.getShip().getCos();
            float currentBeamRange = object.getCurrentBeamRange();

            float beamX = cos * currentBeamRange;
            float beamY = sin * currentBeamRange;
            float x1 = position.x;
            float y1 = position.y;
            float x2 = position.x + beamX;
            float y2 = position.y + beamY;

            float minX, minY, maxX, maxY;

            if (x1 < x2) {
                minX = x1;
                maxX = x2;
            } else {
                minX = x2;
                maxX = x1;
            }

            if (y1 < y2) {
                minY = y1;
                maxY = y2;
            } else {
                minY = y2;
                maxY = y1;
            }

            aabb.union(minX, minY, maxX, maxY);
        }
    }

    @Override
    public void renderAdditive(float lastSin, float lastCos, float sin, float cos) {
        if (effectsColor.w > 0.0f) {
            Vector2f position = object.getPosition();
            Vector2f size = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                    sin, cos, lastSize.x * 2.5f, lastSize.y * 2.5f, size.x * 2.5f, size.y * 2.5f, effectsColor.x,
                    effectsColor.y, effectsColor.z, (lastColor.w + (effectsColor.w - lastColor.w)
                            * Engine.renderer.getInterpolation()) * 0.6f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);

            beam.render(spriteRenderer, lastSin, lastCos, sin, cos);
        }
    }

    @Override
    public void onShot() {
        beam.init();
        weaponSpawnAccumulator.resetTime();
        damageSpawnAccumulator.resetTime();
        Vector2f position = object.getPosition();
        playSounds(object.getGunData(), object.getShip().getWorld().getRand(), position.x, position.y);
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
    }
}