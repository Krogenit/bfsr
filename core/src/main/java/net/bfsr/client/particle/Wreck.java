package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.InstancedRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Wreck extends CollisionObject implements TOITransformSavable {
    @Getter
    private float alphaVelocity;
    @Getter
    private int textureOffset;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    private boolean fireExplosion;
    protected boolean changeFire;
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

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a,
                      float alphaVelocity, Texture texture, int textureOffset, boolean fire, boolean light, boolean fireExplosion, float hull, int destroyedShipId,
                      Random random, float fireR, float fireG, float fireB, float fireA, Texture textureFire, Texture textureLight, float timerLight1) {
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
        this.lastColor.set(color);
        this.alphaVelocity = alphaVelocity;
        this.texture = texture;
        this.textureOffset = textureOffset;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        this.light = light;
        this.hull = hull;
        this.colorFire.set(fireR, fireG, fireB, fireA);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(0.0f);
        this.lastColorLight.set(colorLight);
        this.destroyedShipId = destroyedShipId;
        this.random = random;
        this.textureFire = textureFire;
        this.textureLight = textureLight;
        this.sparkleActivationTimer = timerLight1;
        this.aliveTimer = 0;
        createBody(x, y, angularVelocity);
        createAABB();
        addParticle();
        return this;
    }

    public Wreck init(World world, int id, int textureOffset, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation,
                      float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity) {
        return init(world, id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, textureOffset, fire, light,
                fireExplosion, 10, 0, null, 0.0f, 0.0f, 0.0f, 0.0f, null, null, 0);
    }

    public Wreck init(int textureOffset, boolean isWreck, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                      float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, int id) {
        return init(Core.get().getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity,
                TextureLoader.getTexture(getDebrisTexture(textureOffset, isWreck)), textureOffset, fire, isWreck,
                fireExplosion, 10, 0, Core.get().getWorld().getRand(), isWreck || fire ? 1.0f : 0.0f, isWreck || fire ? 1.0f : 0.0f,
                isWreck || fire ? 1.0f : 0.0f, isWreck || fire ? 1.0f : 0.0f,
                isWreck ? TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckFire0.ordinal() + textureOffset]) : fire ?
                        TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleDerbisEmber1.ordinal() + textureOffset]) : null,
                isWreck ? TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckLight0.ordinal() + textureOffset]) : null,
                isWreck ? 200.0f + Core.get().getWorld().getRand().nextInt(200) : 0.0f);
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
        if (light) {
            Vector2[] vertecies = null;
            if (textureOffset == 0) {
                vertecies = new Vector2[4];
                vertecies[0] = new Vector2(7.21f, 9.16f);
                vertecies[1] = new Vector2(-9.19f, 4.76f);
                vertecies[2] = new Vector2(-7.99f, -10.44f);
                vertecies[3] = new Vector2(8.81f, -7.24f);
            } else if (textureOffset == 1) {
                vertecies = new Vector2[4];
                vertecies[0] = new Vector2(-0.79f, 11.56f);
                vertecies[1] = new Vector2(-7.99f, -0.44f);
                vertecies[2] = new Vector2(3.61f, -10.84f);
                vertecies[3] = new Vector2(6.81f, 1.56f);
            } else if (textureOffset == 2) {
                vertecies = new Vector2[5];
                vertecies[0] = new Vector2(-5.16f, 7.97f);
                vertecies[1] = new Vector2(-11.96f, -0.03f);
                vertecies[2] = new Vector2(-5.96f, -9.23f);
                vertecies[3] = new Vector2(7.24f, -11.23f);
                vertecies[4] = new Vector2(8.84f, 1.97f);
            }

            for (int i = 0; i < vertecies.length; i++) {
                Vector2 vertex = vertecies[i];
                vertex.divide(26.0f);
                vertex.x *= scale.x;
                vertex.y *= scale.y;
            }
            Polygon p = Geometry.createPolygon(vertecies);
            BodyFixture bodyFixture = new BodyFixture(p);
            bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
            bodyFixture.setFilter(new WreckFilter(this));
            body.addFixture(bodyFixture);
        } else {
            Vector2[] vertecies = null;
            if (textureOffset == 0) {
                vertecies = new Vector2[4];
                vertecies[0] = new Vector2(3.40f, 10.93f);
                vertecies[1] = new Vector2(-11.00f, -10.27f);
                vertecies[2] = new Vector2(4.60f, -9.07f);
                vertecies[3] = new Vector2(12.60f, 4.93f);
            } else if (textureOffset == 1) {
                vertecies = new Vector2[4];
                vertecies[0] = new Vector2(7.94f, 5.84f);
                vertecies[1] = new Vector2(-12.26f, -4.51f);
                vertecies[2] = new Vector2(-9.56f, -7.29f);
                vertecies[3] = new Vector2(12.15f, -0.43f);
            } else if (textureOffset == 2) {
                vertecies = new Vector2[5];
                vertecies[0] = new Vector2(-3.61f, 9.74f);
                vertecies[1] = new Vector2(-11.21f, -6.66f);
                vertecies[2] = new Vector2(-2.51f, -13.46f);
                vertecies[3] = new Vector2(11.89f, -7.86f);
                vertecies[4] = new Vector2(8.29f, 9.34f);
            } else if (textureOffset == 3) {
                vertecies = new Vector2[4];
                vertecies[0] = new Vector2(-6.88f, 8.32f);
                vertecies[1] = new Vector2(-8.08f, -2.88f);
                vertecies[2] = new Vector2(-0.48f, -10.48f);
                vertecies[3] = new Vector2(6.72f, 3.92f);
            } else if (textureOffset == 4) {
                vertecies = new Vector2[5];
                vertecies[0] = new Vector2(-2.48f, 12.80f);
                vertecies[1] = new Vector2(-12.88f, 1.20f);
                vertecies[2] = new Vector2(-6.08f, -10.00f);
                vertecies[3] = new Vector2(12.72f, -8.00f);
                vertecies[4] = new Vector2(4.32f, 12.00f);
            } else if (textureOffset == 5) {
                vertecies = new Vector2[5];
                vertecies[0] = new Vector2(-6.88f, 11.20f);
                vertecies[1] = new Vector2(-12.48f, -6.00f);
                vertecies[2] = new Vector2(2.72f, -13.60f);
                vertecies[3] = new Vector2(13.52f, -4.80f);
                vertecies[4] = new Vector2(6.72f, 12.40f);
            }

            for (int i = 0; i < vertecies.length; i++) {
                Vector2 vertex = vertecies[i];
                vertex.divide(30.0f);
                vertex.x *= scale.x;
                vertex.y *= scale.y;
            }
            Polygon p = Geometry.createPolygon(vertecies);
            BodyFixture bodyFixture = new BodyFixture(p);
            bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
            bodyFixture.setFilter(new WreckFilter(this));
            body.addFixture(bodyFixture);
        }
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

        if (fire && world.isRemote()) {
            updateFireAndExplosion();
        }

        if (light && world.isRemote()) {
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
        updateExplosion();
        updateFire();
    }

    protected void updateFire() {
        float fireSpeed = 0.180f * TimeUtils.UPDATE_DELTA_TIME;
        float fireAddSpeed = 0.0300f * TimeUtils.UPDATE_DELTA_TIME;

        if (changeFire) {
            if (colorFire.w > 0.6f) {
                colorFire.w -= fireSpeed - fireAddSpeed;
                colorFire.x -= fireSpeed;
                colorFire.y -= fireSpeed;
                colorFire.z -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.set(0.0f);
                }
            } else {
                changeFire = false;
            }
        } else {
            if (colorFire.w < 1.0f) {
                colorFire.w += fireSpeed + fireAddSpeed;
                colorFire.x += fireSpeed;
                colorFire.y += fireSpeed;
                colorFire.z += fireSpeed;
            } else {
                changeFire = true;
            }
        }

        if (color.w <= 0.5f) {
            if (colorFire.w > 0.0f) {
                fireSpeed = 0.120F * TimeUtils.UPDATE_DELTA_TIME;

                fire = false;
                colorFire.w -= fireSpeed;
                colorFire.x -= fireSpeed;
                colorFire.y -= fireSpeed;
                colorFire.z -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.set(0.0f);
                }
            }
        }
    }

    protected void updateExplosion() {
        if (fireExplosion) {
            explosionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (explosionTimer <= 0 && color.w > 0.6f) {
                float size = scale.x / 4.0f;
                Vector2f position = getPosition();
                ParticleSpawner.spawnExplosion(position.x, position.y, size / 10.0f);
                ParticleSpawner.spawnDamageSmoke(position.x, position.y, size + 1.0f);
                explosionTimer = 8 + world.getRand().nextInt(8);
            }
        }
    }

    protected void updateSparkle() {
        float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;
        sparkleActivationTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (sparkleActivationTimer <= 0.0f) {
            if (changeLight) {
                if (colorLight.w > 0.0f) {
                    colorLight.w -= lightSpeed;
                    colorLight.x -= lightSpeed;
                    colorLight.y -= lightSpeed;
                    colorLight.z -= lightSpeed;
                    if (colorLight.w < 0.0f) {
                        colorLight.set(0.0f);
                    }
                } else {
                    changeLight = false;
                    sparkleBlinkTimer += 25.0f;
                }
            } else {
                if (colorLight.w < color.w) {
                    colorLight.w += lightSpeed;
                    colorLight.x += lightSpeed;
                    colorLight.y += lightSpeed;
                    colorLight.z += lightSpeed;
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

        updateSparkleFading();
    }

    protected void updateSparkleFading() {
        if (color.w < 0.3f) {
            if (colorLight.w > 0.0f) {
                float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;

                light = false;
                colorLight.w -= lightSpeed;
                colorLight.x -= lightSpeed;
                colorLight.y -= lightSpeed;
                colorLight.z -= lightSpeed;
                if (colorLight.w < 0.0f) {
                    colorLight.set(0.0f);
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
        InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive() {
        if (colorFire.w > 0) {
            Vector2f position = getPosition();
            InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    lastColorFire, colorFire, textureFire, BufferType.ENTITIES_ADDITIVE);
        }

        if (colorLight.w > 0) {
            Vector2f position = getPosition();
            InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    lastColorLight, colorLight, textureLight, BufferType.ENTITIES_ADDITIVE);
        }
    }

    private static TextureRegister getDebrisTexture(int textureOffset, boolean isWreck) {
        if (isWreck) {
            return TextureRegister.values()[TextureRegister.particleWreck0.ordinal() + textureOffset];
        } else {
            return TextureRegister.values()[TextureRegister.particleDerbis1.ordinal() + textureOffset];
        }
    }

    public void onRemoved() {
        ParticleSpawner.PARTICLE_WREAK_POOL.returnBack(this);
        Core.get().getWorld().removePhysicObject(this);
    }
}
