package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Wreck extends CollisionObject implements TOITransformSavable {
    @Getter
    private float alphaVelocity;
    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    private boolean fireExplosion;
    protected boolean fireFadingOut;
    private boolean changeLight;

    @Getter
    protected float explosionTimer, sparkleBlinkTimer, sparkleActivationTimer, hull;

    @Getter
    private Texture textureFire, textureLight;

    @Getter
    protected final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    protected final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();

    protected Random random;

    @Getter
    private int destroyedShipId;
    /**
     * Saved transform before TOI solver
     */
    private final Transform transform = new Transform();
    private boolean transformSaved;
    @Getter
    private WreckType wreckType;
    protected RegisteredShipWreck registeredShipWreck;

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float r, float g, float b,
                      float a, float alphaVelocity, String texture, int wreckIndex, boolean fire, boolean light, boolean fireExplosion, float hull, int destroyedShipId,
                      String textureFire, String textureLight, WreckType wreckType, RegisteredShipWreck registeredShipWreck) {
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
        this.aliveTimer = 0;
        this.wreckType = wreckType;
        this.registeredShipWreck = registeredShipWreck;

        if (world.isRemote()) {
            this.lastColor.set(color);
            this.colorFire.set(fire ? 1.0f : 0.0f);
            this.lastColorFire.set(colorFire);
            this.colorLight.set(1.0f, 1.0f, 1.0f, 0.0f);
            this.lastColorLight.set(colorLight);
            this.texture = TextureLoader.getTexture(texture);
            this.textureFire = TextureLoader.getTexture(textureFire);
            this.textureLight = textureLight != null ? TextureLoader.getTexture(textureLight) : null;
            this.sparkleActivationTimer = light ? 200.0f + world.getRand().nextInt(200) : 0.0f;
        }

        createBody(x, y, angularVelocity);
        createAABB();
        addParticle();
        return this;
    }

    public Wreck init(World world, int id, int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation,
                      float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, WreckType wreckType) {
        return init(world, id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, wreckIndex, fire, light,
                fireExplosion, 10, 0, null, null, wreckType, WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex));
    }

    public Wreck init(int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                      float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, int id, WreckType wreckType) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex);
        init(Core.get().getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, wreck.getTexture(),
                wreckIndex, fire, light, fireExplosion, 10, 0, wreck.getFireTexture(),
                wreck.getSparkleTexture(), wreckType, wreck);
        return this;
    }

    protected void addParticle() {
        if (world.isRemote()) {
            Core.get().getWorld().getParticleManager().addParticle(this);
        } else {
            ((WorldServer) world).addWreck(this);
        }
    }

    @Override
    protected void createBody(float x, float y) {
        createBody(x, y, 0.0f);
    }

    protected void createBody(float x, float y, float angularVelocity) {
        createFixtures();
        body.translate(x, y);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rotation);
        body.setAngularVelocity(angularVelocity);
        setLinearAndAngularDamping();

        world.addPhysicObject(this);
    }

    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    protected void createFixtures() {
        if (body.getFixtures().size() > 0) body.removeFixture(0);
        Polygon p = Geometry.scale(registeredShipWreck.getPolygon(), scale.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    public void setDead(boolean isDead) {
        Vector2f velocity = getVelocity();
        if (world.isRemote()) {
            if (color.w > 0.01f) {
                Vector2f pos = getPosition();
                ParticleSpawner.spawnLight(pos.x, pos.y, getScale().x * 2.0f, 1.0f, 0.8f, 0.6f, 1.0f, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnSpark(pos.x, pos.y, getScale().x);
                ParticleSpawner.spawnExplosion(pos.x, pos.y, getScale().x);
                ParticleSpawner.spawnSmallGarbage(random.nextInt(10), pos.x, pos.y, 2.0f, 5.0f + getScale().x);
                ParticleSpawner.spawnShipOst(random.nextInt(3), pos.x, pos.y, velocity.x * 0.06f, velocity.y * 0.06f, 0.25f + 0.75f * random.nextFloat());
            }
        }

        super.setDead(isDead);
    }

    @Override
    public void update() {
        lastSin = sin;
        lastCos = cos;
        lastPosition.set(getPosition());
        lastColor.w = color.w;
        lastColorFire.set(colorFire);
        lastColorLight.set(colorLight);

        if (world.isRemote()) {
            aliveTimer += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (aliveTimer > 120) {
                setDead(true);
                aliveTimer = 0;
            }
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        if (world.isRemote()) {
            updateFireAndExplosion();
            updateSparkle();
        }

        updateLifeTime();
    }

    protected void updateLifeTime() {
        if (color.w < 0.2f) {
            color.w -= alphaVelocity * 0.05f;
            if (color.w <= 0) {
                color.w = 0;
                if (!world.isRemote()) {
                    MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                    setDead(true);
                }
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

    public void postPhysicsUpdate() {
        if (transformSaved) {
            body.setTransform(transform);
            transformSaved = false;
        }

        super.postPhysicsUpdate();
    }

    public void damage(float damage) {
        hull -= damage;
        if (!world.isRemote() && hull <= 0) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            setDead(true);
        }
    }

    @Override
    public void saveTransform(Transform transform) {
        this.transform.set(transform);
        transformSaved = true;
    }

    @Override
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
