package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.particle.effect.FireEffects;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WreckRender extends RigidBodyRender {
    private final Wreck wreck;
    @Getter
    private final AbstractTexture textureFire, textureLight;

    private boolean changeLight;
    @Getter
    private final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    private final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();
    private boolean fireFadingOut;
    private int sparkleActivationTimerInTicks;
    private boolean fire;
    private boolean light;
    private float sparkleBlinkTimer;

    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();
    private final float fireAnimationSpeed = Core.get().convertToDeltaTime(0.18f);
    private final float lightAnimationSpeed = Core.get().convertToDeltaTime(12.0f);

    public WreckRender(Wreck object) {
        super(Engine.assetsManager.getTexture(object.getConfigData().getTexture()), object, 0.5f, 0.5f, 0.5f, 1.0f);
        this.wreck = object;

        if (object.isEmitFire()) {
            spawnAccumulator.resetTime();
        }

        WreckData wreckData = object.getWreckData();
        this.textureFire = Engine.assetsManager.getTexture(wreckData.getFireTexture());
        this.textureLight =
                wreckData.getSparkleTexture() != null ? Engine.assetsManager.getTexture(wreckData.getSparkleTexture()) : null;
        this.colorFire.set(object.isFire() ? 1.0f : 0.0f);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(1.0f, 1.0f, 1.0f, 0.0f);
        this.lastColorLight.set(colorLight);
        this.sparkleActivationTimerInTicks = object.isLight() ?
                Core.get().convertToTicks(200.0f + object.getWorld().getRand().nextInt(200)) : 0;
        this.fire = object.isFire();
        this.light = object.isLight();

        object.getWreckEventBus().register(this);
    }

    @Override
    public void update() {
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);
        lastSin = wreck.getSin();
        lastCos = wreck.getCos();
        lastPosition.set(object.getPosition());

        updateLifeTime();
        updateFireAndExplosion();
        updateSparkle();
    }

    private void updateLifeTime() {
        color.w = 1.0f - wreck.getLifeTime() / (float) wreck.getMaxLifeTime();
    }

    private void emitFire() {
        if (color.w > 0.6f) {
            Vector2f position = object.getPosition();
            FireEffects.emitFire(position.x, position.y, spawnAccumulator);
        }
    }

    private void updateFireAndExplosion() {
        if (wreck.isEmitFire()) {
            emitFire();
        }

        updateFire();
    }

    private void updateFire() {
        if (fire) {
            if (fireFadingOut) {
                if (colorFire.w > 0.4f) {
                    colorFire.w -= fireAnimationSpeed;
                    if (colorFire.w < 0.0f) {
                        colorFire.w = 0.0f;
                    }
                } else {
                    fireFadingOut = false;
                }
            } else {
                if (colorFire.w < 1.0f) {
                    colorFire.w += fireAnimationSpeed;
                } else {
                    fireFadingOut = true;
                }
            }
        }

        if (color.w <= 0.5f) {
            if (colorFire.w > 0.0f) {
                float fireSpeed = fireAnimationSpeed * 4.0f;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }
            }
        }
    }

    private void updateSparkle() {
        if (light) {
            sparkleActivationTimerInTicks -= 1;
            if (sparkleActivationTimerInTicks <= 0.0f) {
                if (changeLight) {
                    if (colorLight.w > 0.0f) {
                        colorLight.w -= lightAnimationSpeed;
                        if (colorLight.w < 0.0f) {
                            colorLight.w = 0.0f;
                        }
                    } else {
                        changeLight = false;
                        sparkleBlinkTimer += 25.0f;
                    }
                } else {
                    if (colorLight.w < color.w) {
                        colorLight.w += lightAnimationSpeed;
                    } else {
                        changeLight = true;
                        sparkleBlinkTimer += 25.0f;
                    }
                }
            }

            if (sparkleBlinkTimer >= 100.0f) {
                sparkleActivationTimerInTicks = Core.get().convertToTicks(200.0f + wreck.getWorld().getRand().nextInt(200));
                sparkleBlinkTimer = 0.0f;
            }
        }

        updateSparkleFading();
    }

    private void updateSparkleFading() {
        if (color.w < 0.3f) {
            if (colorLight.w > 0.0f) {
                light = false;
                colorLight.w -= lightAnimationSpeed;
                if (colorLight.w < 0.0f) {
                    colorLight.w = 0.0f;
                }
            }
        }
    }

    @Override
    public void renderAlpha() {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                wreck.getSin(), wreck.getCos(), size.x, size.y, color.x, color.y, color.z, color.w, texture,
                BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderAdditive() {
        if (colorFire.w > 0) {
            Vector2f position = object.getPosition();
            Vector2f size = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                    wreck.getSin(), wreck.getCos(), size.x, size.y, lastColorFire, colorFire, textureFire,
                    BufferType.ENTITIES_ADDITIVE);
        }

        if (colorLight.w > 0) {
            Vector2f position = object.getPosition();
            Vector2f size = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                    wreck.getSin(), wreck.getCos(), size.x, size.y, lastColorLight, colorLight, textureLight,
                    BufferType.ENTITIES_ADDITIVE);
        }
    }

    @EventHandler
    public EventListener<WreckDeathEvent> wreckDeathEventEvent() {
        return event -> {
            Wreck wreck = event.wreck();
            if (color.w > 0.01f) {
                Vector2f pos = wreck.getPosition();
                ExplosionEffects.spawnSmallExplosion(pos.x, pos.y, wreck.getSize().x);
            }
        };
    }
}