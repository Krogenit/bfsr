package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.particle.effect.FireEffects;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
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
    private final float fireAnimationSpeed = Client.get().convertToDeltaTime(0.18f);
    private final float lightAnimationSpeed = Client.get().convertToDeltaTime(12.0f);

    private int fireId = -1;
    private int lightId = -1;

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
                Client.get().convertToTicks(200.0f + object.getWorld().getRand().nextInt(200)) : 0;
        this.fire = object.isFire();
        this.light = object.isLight();

        object.getWreckEventBus().register(this);
    }

    @Override
    public void init() {
        super.init();
        if (fire) {
            fireId = spriteRenderer.add(wreck.getX(), wreck.getY(), wreck.getSin(), wreck.getCos(), wreck.getSizeX(), wreck.getSizeY(),
                    colorFire.x, colorFire.y, colorFire.z, colorFire.w, textureFire.getTextureHandle(), BufferType.ENTITIES_ADDITIVE);
        }

        if (light) {
            lightId = spriteRenderer.add(wreck.getX(), wreck.getY(), wreck.getSin(), wreck.getCos(), wreck.getSizeX(), wreck.getSizeY(),
                    colorLight.x, colorLight.y, colorLight.z, colorLight.w, textureLight.getTextureHandle(), BufferType.ENTITIES_ADDITIVE);
        }
    }

    @Override
    public void update() {
        super.update();

        updateLifeTime();
        updateFireAndExplosion();
        updateSparkle();
    }

    @Override
    protected void updateLastRenderValues() {
        super.updateLastRenderValues();
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);
        spriteRenderer.setLastColorAlpha(id, BufferType.ENTITIES_ALPHA, color.w);

        if (fire) {
            spriteRenderer.setLastPosition(fireId, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
            spriteRenderer.setLastRotation(fireId, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
            spriteRenderer.setLastColor(fireId, BufferType.ENTITIES_ADDITIVE, colorFire);
        }

        if (light) {
            spriteRenderer.setLastPosition(lightId, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
            spriteRenderer.setLastRotation(lightId, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
            spriteRenderer.setLastColor(lightId, BufferType.ENTITIES_ADDITIVE, colorLight);
        }
    }

    @Override
    protected void updateRenderValues() {
        super.updateRenderValues();
        spriteRenderer.setColorAlpha(id, BufferType.ENTITIES_ALPHA, color.w);

        if (fire) {
            spriteRenderer.setPosition(fireId, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
            spriteRenderer.setRotation(fireId, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
        }

        if (light) {
            spriteRenderer.setPosition(lightId, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
            spriteRenderer.setRotation(lightId, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
        }
    }

    private void updateLifeTime() {
        color.w = 1.0f - wreck.getLifeTime() / (float) wreck.getMaxLifeTime();
    }

    private void emitFire() {
        if (color.w > 0.6f) {
            FireEffects.emitFire(object.getX(), object.getY(), spawnAccumulator);
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

            spriteRenderer.setColor(fireId, BufferType.ENTITIES_ADDITIVE, colorFire);
        }

        if (color.w <= 0.5f) {
            if (colorFire.w > 0.0f) {
                float fireSpeed = fireAnimationSpeed * 4.0f;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }

                spriteRenderer.setColor(fireId, BufferType.ENTITIES_ADDITIVE, colorFire);
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
                sparkleActivationTimerInTicks = Client.get().convertToTicks(200.0f + wreck.getWorld().getRand().nextInt(200));
                sparkleBlinkTimer = 0.0f;
            }

            spriteRenderer.setColor(lightId, BufferType.ENTITIES_ADDITIVE, colorLight);
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

                spriteRenderer.setColor(lightId, BufferType.ENTITIES_ADDITIVE, colorLight);
            }
        }
    }

    @Override
    public void renderAdditive() {
        if (colorFire.w > 0) {
            spriteRenderer.addDrawCommand(fireId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ADDITIVE);
        }

        if (colorLight.w > 0) {
            spriteRenderer.addDrawCommand(lightId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ADDITIVE);
        }
    }

    @EventHandler
    public EventListener<WreckDeathEvent> wreckDeathEventEvent() {
        return event -> {
            Wreck wreck = event.wreck();
            if (color.w > 0.01f) {
                ExplosionEffects.spawnSmallExplosion(wreck.getX(), wreck.getY(), wreck.getSizeX());
            }
        };
    }

    @Override
    public void clear() {
        super.clear();

        if (fireId != -1) {
            spriteRenderer.removeObject(fireId, BufferType.ENTITIES_ADDITIVE);
        }

        if (lightId != -1) {
            spriteRenderer.removeObject(lightId, BufferType.ENTITIES_ADDITIVE);
        }
    }
}