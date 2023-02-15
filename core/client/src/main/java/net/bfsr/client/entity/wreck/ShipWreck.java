package net.bfsr.client.entity.wreck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.ShipWreckCommon;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.util.TimeUtils;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

@NoArgsConstructor
public class ShipWreck extends ShipWreckCommon {
    @Getter
    private Texture texture, textureFire, textureLight;

    @Getter
    protected final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    protected final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();

    protected float sparkleActivationTimer;
    private boolean changeLight;

    public ShipWreck init(int id, int wreckIndex, ShipCommon ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float lifeTime) {
        super.init(id, wreckIndex, ship, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, lifeTime);
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        this.texture = TextureLoader.getTexture(wreck.getTexture());
        this.textureFire = TextureLoader.getTexture(wreck.getFireTexture());
        this.textureLight = TextureLoader.getTexture(wreck.getSparkleTexture());
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

    protected void updateFire() {
        if (fire) {
            float fireSpeed = 0.120f * TimeUtils.UPDATE_DELTA_TIME;

            if (fireFadingOut) {
                if (colorFire.w > 0.7f) {
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

        if (lifeTime <= maxLifeTime / 2.0f) {
            if (colorFire.w > 0.0f) {
                float fireSpeed = 0.120F * TimeUtils.UPDATE_DELTA_TIME;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }
            }
        }
    }

    protected void updateSparkleFading() {
        if (lifeTime <= maxLifeTime / 7.0f) {
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
        if (color.w > 0.01f) {
            Vector2f velocity = getVelocity();
            Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
            Vector2f scale = getScale();
            ParticleSpawner.spawnLight((float) worldPos.x, (float) worldPos.y, getScale().x * 2.0f, 1.0f, 0.8f, 0.6f, 1.0f, RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnSpark((float) worldPos.x, (float) worldPos.y, getScale().x);
            ParticleSpawner.spawnExplosion((float) worldPos.x, (float) worldPos.y, getScale().x);
            ParticleSpawner.spawnSmallGarbage(random.nextInt(10), (float) worldPos.x, (float) worldPos.y, 2.0f, 5.0f + getScale().x);
            ParticleSpawner.spawnShipOst(random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x * 0.06f, velocity.y * 0.06f, 0.25f + 0.75f * random.nextFloat());

            ParticleSpawner.spawnMediumGarbage(3, (float) worldPos.x, (float) worldPos.y, velocity.x * 0.1f, velocity.y * 0.1f, scale.x / 2.0f);
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, (float) worldPos.x, (float) worldPos.y));
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
        ParticleSpawner.PARTICLE_SHIP_WREAK_POOL.returnBack(this);
        Core.get().getWorld().removePhysicObject(this);
    }
}
