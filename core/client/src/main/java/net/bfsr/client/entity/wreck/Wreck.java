package net.bfsr.client.entity.wreck;

import lombok.Getter;
import net.bfsr.client.collision.filter.WreckFilter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Wreck extends CollisionObject {
    @Getter
    protected float alphaVelocity;
    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    protected boolean fireExplosion;
    protected boolean fireFadingOut;

    @Getter
    protected float explosionTimer, sparkleBlinkTimer, hull;

    protected Random random;

    @Getter
    private int destroyedShipId;

    @Getter
    private WreckType wreckType;
    protected RegisteredShipWreck registeredShipWreck;

    @Getter
    private Texture textureFire, textureLight;

    @Getter
    protected final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    protected final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();

    protected float sparkleActivationTimer;
    private boolean changeLight;

    public Wreck init(WorldClient world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float r, float g, float b,
                      float a, float alphaVelocity, int wreckIndex, boolean fire, boolean light, boolean fireExplosion, float hull, int destroyedShipId,
                      WreckType wreckType, RegisteredShipWreck registeredShipWreck) {
        this.world = world;
        this.id = id;
        this.position.set(x, y);
        this.lastPosition.set(position);
        this.velocity.set(velocityX, velocityY);
        this.rotation = rotation;
        this.lastRotation = rotation;
        this.scale.set(scaleX, scaleY);
        this.lastScale.set(scale);
        this.color.set(r, g, b, a);
        this.alphaVelocity = alphaVelocity;
        this.wreckIndex = wreckIndex;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        this.light = light;
        this.hull = hull;
        this.destroyedShipId = destroyedShipId;
        this.random = world.getRand();
        this.lifeTime = 0;
        this.wreckType = wreckType;
        this.registeredShipWreck = registeredShipWreck;
        this.isDead = false;
        this.lastColor.set(color);
        this.colorFire.set(fire ? 1.0f : 0.0f);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(1.0f, 1.0f, 1.0f, 0.0f);
        this.lastColorLight.set(colorLight);
        this.texture = TextureLoader.getTexture(registeredShipWreck.getTexture());
        this.textureFire = TextureLoader.getTexture(registeredShipWreck.getFireTexture());
        this.textureLight = registeredShipWreck.getSparkleTexture() != null ? TextureLoader.getTexture(registeredShipWreck.getSparkleTexture()) : null;
        this.sparkleActivationTimer = light ? 200.0f + world.getRand().nextInt(200) : 0.0f;
        createFixtures(angularVelocity);
        world.getParticleManager().addParticle(this);
        world.addPhysicObject(this);
        return this;
    }

    public Wreck init(int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                      float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, int id, WreckType wreckType) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex);
        return init(Core.get().getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, wreckIndex, fire, light, fireExplosion, 10,
                0, wreckType, wreck);
    }

    @Override
    protected void initBody() {
        createFixtures(0.0f);
    }

    protected void createFixtures(float angularVelocity) {
        while (body.getFixtures().size() > 0) body.removeFixture(0);
        createFixture();
        body.translate(position.x, position.y);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rotation);
        body.setAngularVelocity(angularVelocity);
        setLinearAndAngularDamping();
    }

    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    protected void createFixture() {
        Polygon p = Geometry.scale(registeredShipWreck.getPolygon(), scale.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    public void update() {
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);
        lastSin = sin;
        lastCos = cos;
        lastPosition.set(getPosition());

        updateLifeTime();
        updateFireAndExplosion();
        updateSparkle();
    }

    protected void updateLifeTime() {
        if (color.w < 0.2f) {
            color.w -= alphaVelocity * 0.05f;
            if (color.w <= 0.0f) {
                color.w = 0.0f;
            }
        } else {
            color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
            if (color.w < 0.0f) color.w = 0.0f;
        }
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

    public void damage(float damage) {
        hull -= damage;
    }

    @Override
    public void setDead() {
        super.setDead();
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

    public void render() {
        Vector2f position = getPosition();
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

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

    public void onRemoved() {
        ParticleSpawner.PARTICLE_WREAK_POOL.returnBack(this);
        Core.get().getWorld().removePhysicObject(this);
    }
}