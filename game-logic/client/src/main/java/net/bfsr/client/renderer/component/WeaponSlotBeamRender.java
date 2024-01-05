package net.bfsr.client.renderer.component;

import lombok.Getter;
import net.bfsr.client.particle.Beam;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.math.RotationHelper;
import org.dyn4j.collision.narrowphase.Raycast;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponSlotBeamRender extends WeaponSlotRender<WeaponSlotBeam> {
    private static final AbstractTexture LIGHT_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleLight);

    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();
    private final Beam beam;
    private boolean maxPower;
    @Getter
    private final Vector4f effectsColor = new Vector4f();

    public WeaponSlotBeamRender(WeaponSlotBeam object) {
        super(object);
        this.effectsColor.set(object.getGunData().getColor());
        this.effectsColor.w = 0.0f;
        this.beam = new Beam(object, effectsColor);
    }

    @Override
    public void update() {
        super.update();
        beam.update();

        if (object.getReloadTimer() > 0) {
            if (object.getReloadTimer() <= object.getTimeToReload() * 0.3333f) {
                maxPower = false;
                if (effectsColor.w > 0.0f) {
                    effectsColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (effectsColor.w < 0) effectsColor.w = 0;
                }
            } else {
                if (!maxPower && effectsColor.w < 1.0f) {
                    effectsColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (effectsColor.w > 1.0f) effectsColor.w = 1.0f;
                } else {
                    maxPower = true;
                }

                if (maxPower) {
                    effectsColor.w = object.getShip().getWorld().getRand().nextFloat() / 3.0f + 0.66f;
                }

                float sin = object.getShip().getSin();
                float cos = object.getShip().getCos();
                Vector2f angleToVelocity = RotationHelper.angleToVelocity(sin, cos, 10.0f);
                Vector2f position = object.getPosition();
                BeamEffects.beam(position.x, position.y, 2.0f, sin, cos, angleToVelocity.x, angleToVelocity.y,
                        effectsColor.x, effectsColor.y, effectsColor.z, effectsColor.w, weaponSpawnAccumulator);
            }
        } else {
            if (effectsColor.w > 0.0f) {
                effectsColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (effectsColor.w < 0) effectsColor.w = 0;
            }
        }
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        if (object.getReloadTimer() > 0) {
            beam.postPhysicsUpdate();
        }
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();

        if (object.getReloadTimer() > 0 && effectsColor.w > 0) {
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
        if (object.getReloadTimer() > 0 && effectsColor.w > 0) {
            if (object.getReloadTimer() > object.getTimeToReload() / 3.0f) {
                Vector2f position = object.getPosition();
                Vector2f size = object.getSize();
                spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                        sin, cos, lastSize.x * 2.5f, lastSize.y * 2.5f, size.x * 2.5f, size.y * 2.5f, effectsColor.x,
                        effectsColor.y, effectsColor.z, 0.6f * effectsColor.w, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
            }

            beam.render(spriteRenderer, lastSin, lastCos, sin, cos);
        }
    }

    @Override
    public void onShot() {
        beam.init();
        weaponSpawnAccumulator.resetTime();
        damageSpawnAccumulator.resetTime();
    }

    public void onDamage(Raycast raycast, float hitX, float hitY) {
        BeamEffects.beamDamage(raycast, hitX, hitY, object.getSize().x, effectsColor, damageSpawnAccumulator);
    }
}