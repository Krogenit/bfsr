package net.bfsr.client.entity.wreck;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Wreck extends WreckCommon {
    @Getter
    private Texture texture, textureFire, textureLight;

    @Getter
    protected final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    protected final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();

    protected float sparkleActivationTimer;
    private boolean changeLight;

    public Wreck init(int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                      float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, int id, WreckType wreckType) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex);
        init(Core.get().getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, wreckIndex, fire, light, fireExplosion, 10,
                0, wreckType, wreck);
        this.lastColor.set(color);
        this.colorFire.set(fire ? 1.0f : 0.0f);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(1.0f, 1.0f, 1.0f, 0.0f);
        this.lastColorLight.set(colorLight);
        this.texture = TextureLoader.getTexture(wreck.getTexture());
        this.textureFire = TextureLoader.getTexture(wreck.getFireTexture());
        this.textureLight = wreck.getSparkleTexture() != null ? TextureLoader.getTexture(wreck.getSparkleTexture()) : null;
        this.sparkleActivationTimer = light ? 200.0f + world.getRand().nextInt(200) : 0.0f;
        return this;
    }

    @Override
    protected void addParticle() {
        Core.get().getWorld().getParticleManager().addParticle(this);
    }

    @Override
    public void update() {
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);
        lastSin = sin;
        lastCos = cos;
        lastPosition.set(getPosition());
        super.update();

        aliveTimer += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (aliveTimer > 120) {
            destroy();
            aliveTimer = 0;
        }

        updateFireAndExplosion();
        updateSparkle();
    }

    protected void updateFireAndExplosion() {
        if (fireExplosion) {
            updateExplosion();
        }

        updateFire();
    }

    protected void updateFire() {
        if (fire) {
            float fireSpeed = 0.180f * TimeUtils.UPDATE_DELTA_TIME;

            if (fireFadingOut) {
                if (colorFire.w > 0.4f) {
                    colorFire.w -= fireSpeed;
                    if (colorFire.w < 0.0f) {
                        colorFire.w = 0.0f;
                    }
                } else {
                    fireFadingOut = false;
                }
            } else {
                if (colorFire.w < 1.0f) {
                    colorFire.w += fireSpeed;
                } else {
                    fireFadingOut = true;
                }
            }
        }

        if (color.w <= 0.5f) {
            if (colorFire.w > 0.0f) {
                float fireSpeed = alphaVelocity * 4.0f * TimeUtils.UPDATE_DELTA_TIME;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }
            }
        }
    }

    protected void updateExplosion() {
        explosionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (explosionTimer <= 0 && color.w > 0.6f) {
            float size = scale.x / 4.0f;
            Vector2f position = getPosition();
            ParticleSpawner.spawnExplosion(position.x, position.y, size / 10.0f);
            ParticleSpawner.spawnDamageSmoke(position.x, position.y, size + 1.0f);
            explosionTimer = 8 + world.getRand().nextInt(8);
        }
    }

    protected void updateSparkle() {
        if (light) {
            float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;
            sparkleActivationTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (sparkleActivationTimer <= 0.0f) {
                if (changeLight) {
                    if (colorLight.w > 0.0f) {
                        colorLight.w -= lightSpeed;
                        if (colorLight.w < 0.0f) {
                            colorLight.w = 0.0f;
                        }
                    } else {
                        changeLight = false;
                        sparkleBlinkTimer += 25.0f;
                    }
                } else {
                    if (colorLight.w < color.w) {
                        colorLight.w += lightSpeed;
                    } else {
                        changeLight = true;
                        sparkleBlinkTimer += 25.0f;
                    }
                }
            }

            if (sparkleBlinkTimer >= 100.0f) {
                sparkleActivationTimer = 200.0f + random.nextInt(200);
                sparkleBlinkTimer = 0.0f;
            }
        }

        updateSparkleFading();
    }

    protected void updateSparkleFading() {
        if (color.w < 0.3f) {
            if (colorLight.w > 0.0f) {
                float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;

                light = false;
                colorLight.w -= lightSpeed;
                if (colorLight.w < 0.0f) {
                    colorLight.w = 0.0f;
                }
            }
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        Vector2f velocity = getVelocity();
        if (color.w > 0.01f) {
            Vector2f pos = getPosition();
            ParticleSpawner.spawnLight(pos.x, pos.y, getScale().x * 2.0f, 1.0f, 0.8f, 0.6f, 1.0f, RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnSpark(pos.x, pos.y, getScale().x);
            ParticleSpawner.spawnExplosion(pos.x, pos.y, getScale().x);
            ParticleSpawner.spawnSmallGarbage(random.nextInt(10), pos.x, pos.y, 2.0f, 5.0f + getScale().x);
            ParticleSpawner.spawnShipOst(random.nextInt(3), pos.x, pos.y, velocity.x * 0.06f, velocity.y * 0.06f, 0.25f + 0.75f * random.nextFloat());
        }
    }

    @Override
    public void render() {
        Vector2f position = getPosition();
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderAdditive() {
        if (colorFire.w > 0) {
            Vector2f position = getPosition();
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    lastColorFire, colorFire, textureFire, BufferType.ENTITIES_ADDITIVE);
        }

        if (colorLight.w > 0) {
            Vector2f position = getPosition();
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    lastColorLight, colorLight, textureLight, BufferType.ENTITIES_ADDITIVE);
        }
    }

    @Override
    public void onRemoved() {
        ParticleSpawner.PARTICLE_WREAK_POOL.returnBack(this);
        Core.get().getWorld().removePhysicObject(this);
    }
}
