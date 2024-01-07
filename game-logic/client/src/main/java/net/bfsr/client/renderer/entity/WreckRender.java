package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.FireEffects;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.wreck.Wreck;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WreckRender extends RigidBodyRender<Wreck> {
    @Getter
    private final AbstractTexture textureFire, textureLight;

    private boolean changeLight;
    @Getter
    private final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    private final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();
    private boolean fireFadingOut;
    private int sparkleActivationTimerInTicks;
    private final float lifeTimeVelocity;
    private boolean fire;
    private boolean light;
    private float sparkleBlinkTimer;

    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();
    private final float fireAnimationSpeed = Engine.convertToDeltaTime(0.18f);
    private final float lightAnimationSpeed = Engine.convertToDeltaTime(12.0f);

    public WreckRender(Wreck object) {
        super(Engine.assetsManager.getTexture(object.getConfigData().getTexture()), object, 0.5f, 0.5f, 0.5f, 1.0f);

        if (object.isEmitFire()) {
            spawnAccumulator.resetTime();
        }

        WreckData wreckData = object.getConfigData();
        this.textureFire = Engine.assetsManager.getTexture(wreckData.getFireTexture());
        this.textureLight =
                wreckData.getSparkleTexture() != null ? Engine.assetsManager.getTexture(wreckData.getSparkleTexture()) : null;
        this.colorFire.set(object.isFire() ? 1.0f : 0.0f);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(1.0f, 1.0f, 1.0f, 0.0f);
        this.lastColorLight.set(colorLight);
        this.sparkleActivationTimerInTicks = object.isLight() ?
                Engine.convertToTicks(200.0f + object.getWorld().getRand().nextInt(200)) : 0;
        this.lifeTimeVelocity = object.getLifeTimeVelocity();
        this.fire = object.isFire();
        this.light = object.isLight();
    }

    @Override
    public void update() {
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);
        lastSin = object.getSin();
        lastCos = object.getCos();
        lastPosition.set(object.getPosition());

        updateLifeTime();
        updateFireAndExplosion();
        updateSparkle();
    }

    protected void updateLifeTime() {
        if (color.w < 0.2f) {
            color.w -= lifeTimeVelocity * 2.0f;
            if (color.w <= 0.0f) {
                color.w = 0.0f;
            }
        } else {
            color.w -= lifeTimeVelocity;
            if (color.w < 0.0f) color.w = 0.0f;
        }
    }

    protected void emitFire() {
        if (color.w > 0.6f) {
            Vector2f position = object.getPosition();
            FireEffects.emitFire(position.x, position.y, spawnAccumulator);
        }
    }

    protected void updateFireAndExplosion() {
        if (object.isEmitFire()) {
            emitFire();
        }

        updateFire();
    }

    protected void updateFire() {
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
                float fireSpeed = lifeTimeVelocity * 4.0f;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }
            }
        }
    }

    protected void updateSparkle() {
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
                sparkleActivationTimerInTicks = Engine.convertToTicks(200.0f + object.getWorld().getRand().nextInt(200));
                sparkleBlinkTimer = 0.0f;
            }
        }

        updateSparkleFading();
    }

    protected void updateSparkleFading() {
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
                object.getSin(), object.getCos(), size.x, size.y, color.x, color.y, color.z, color.w, texture,
                BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderAdditive() {
        if (colorFire.w > 0) {
            Vector2f position = object.getPosition();
            Vector2f size = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                    object.getSin(), object.getCos(), size.x, size.y, lastColorFire, colorFire, textureFire,
                    BufferType.ENTITIES_ADDITIVE);
        }

        if (colorLight.w > 0) {
            Vector2f position = object.getPosition();
            Vector2f size = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                    object.getSin(), object.getCos(), size.x, size.y, lastColorLight, colorLight, textureLight,
                    BufferType.ENTITIES_ADDITIVE);
        }
    }
}