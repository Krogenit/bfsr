package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.render.instanced.BufferType;
import net.bfsr.client.render.instanced.InstancedRenderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
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
    private boolean fire;
    @Getter
    private boolean light;
    @Getter
    private boolean fireExplosion;
    private boolean changeFire;
    private boolean changeLight;
    @Getter
    private boolean shipWreck;

    @Getter
    private float explosionTimer, timerLight, timerLight1, lifeTime, maxLifeTime, hull;

    private TextureRegister textureWreck;
    @Getter
    private Texture textureFire, textureLight;

    @Getter
    private final Vector4f colorFire = new Vector4f(), colorLight = new Vector4f();
    @Getter
    private final Vector4f lastColorFire = new Vector4f(), lastColorLight = new Vector4f();

    private Random random;

    @Getter
    private int destroyedShipId;
    /**
     * Saved transform before TOI solver
     */
    private final Transform transform = new Transform();
    private boolean transformSaved;

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a,
                      float alphaVelocity, Texture texture, int textureOffset, boolean shipWreck, boolean fire, boolean light, boolean fireExplosion, TextureRegister textureWreck,
                      float hull, float lifeTime, int destroyedShipId, Random random, float fireR, float fireG, float fireB, float fireA, Texture textureFire,
                      Texture textureLight, float timerLight1) {
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
        this.shipWreck = shipWreck;
        this.hull = hull;
        this.colorFire.set(fireR, fireG, fireB, fireA);
        this.lastColorFire.set(colorFire);
        this.colorLight.set(0.0f);
        this.lastColorLight.set(colorLight);
        this.textureWreck = textureWreck;
        this.lifeTime = maxLifeTime = lifeTime;
        this.destroyedShipId = destroyedShipId;
        this.random = random;
        this.textureFire = textureFire;
        this.textureLight = textureLight;
        this.timerLight1 = timerLight1;
        this.aliveTimer = 0;
        createBody(x, y, angularVelocity);
        createAABB();

        if (world.isRemote()) {
            addParticle();
        } else {
            ((WorldServer) world).addWreck(this);
        }

        return this;
    }

    public Wreck init(World world, int id, int textureOffset, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation,
                      float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity) {
        return init(world, id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, textureOffset, false, fire, light,
                fireExplosion, null, 10, 0, 0, null, 0.0f, 0.0f, 0.0f, 0.0f, null, null, 0);
    }

    public Wreck init(int id, int textureOffset, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                      float r, float g, float b, float a, float alphaVelocity, float lifeTime) {
        return init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, textureOffset, true, true, true,
                true, ship.getWreckTexture(textureOffset), ship.getHull().getMaxHull() / 4.0f, lifeTime, ship.getId(), ship.getWorld().getRand(), 0.0f, 0.0f, 0.0f, 0.0f, null, null, 0.0f);
    }

    public Wreck init(int textureOffset, boolean isWreck, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                      float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, int id) {
        return init(Core.getCore().getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity,
                TextureLoader.getTexture(getDebrisTexture(textureOffset, isWreck)), textureOffset, false, fire, isWreck,
                fireExplosion, null, 10, 0, 0, Core.getCore().getWorld().getRand(), isWreck || fire ? 1.0f : 0.0f, isWreck || fire ? 1.0f : 0.0f,
                isWreck || fire ? 1.0f : 0.0f, isWreck || fire ? 1.0f : 0.0f,
                isWreck ? TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckFire0.ordinal() + textureOffset]) : fire ?
                        TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleDerbisEmber1.ordinal() + textureOffset]) : null,
                isWreck ? TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckLight0.ordinal() + textureOffset]) : null,
                isWreck ? 200.0f + Core.getCore().getWorld().getRand().nextInt(200) : 0.0f);
    }

    public Wreck init(int textureOffset, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                      float r, float g, float b, float a, float alphaVelocity, int id, float lifeTime) {
        return init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity,
                TextureLoader.getTexture(ship.getWreckTexture(textureOffset)), textureOffset, true, true, true, true, ship.getWreckTexture(textureOffset),
                ship.getHull().getMaxHull() / 4.0f, lifeTime, ship.getId(), ship.getWorld().getRand(), 1.0f, 1.0f, 1.0f, 0.0f,
                TextureLoader.getTexture(ship.getWreckFireTexture(textureOffset)), TextureLoader.getTexture(ship.getWreckLightTexture(textureOffset)),
                200.0f + ship.getWorld().getRand().nextInt(200));
    }

    protected void addParticle() {
        Core.getCore().getWorld().getParticleManager().addParticle(this);
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
        if (shipWreck) {
            body.setLinearDamping(0.2f);
            body.setAngularDamping(0.025f);
        } else {
            body.setLinearDamping(0.05f);
            body.setAngularDamping(0.005f);
        }

        world.addPhysicObject(this);
    }

    protected void createFixtures() {
        if (body.getFixtures().size() > 0) body.removeFixture(0);
        if (shipWreck) {
            Vector2[] vertices;
            if (textureWreck == TextureRegister.particleWreckEngiSmall0Wreck0) {
                vertices = new Vector2[6];
                vertices[0] = new Vector2(-16.80f, 20.20f);
                vertices[1] = new Vector2(-32.80f, 4.60f);
                vertices[2] = new Vector2(-32.00f, -11.40f);
                vertices[3] = new Vector2(-15.60f, -20.20f);
                vertices[4] = new Vector2(-0.40f, -19.00f);
                vertices[5] = new Vector2(-4.00f, 16.60f);
            } else if (textureWreck == TextureRegister.particleWreckEngiSmall0Wreck1) {
                vertices = new Vector2[5];
                vertices[0] = new Vector2(14.00f, 13.40f);
                vertices[1] = new Vector2(-6.80f, -5.00f);
                vertices[2] = new Vector2(7.60f, -21.00f);
                vertices[3] = new Vector2(33.60f, -7.00f);
                vertices[4] = new Vector2(34.40f, 2.60f);
            } else if (textureWreck == TextureRegister.particleWreckHumanSmall0Wreck0) {
                vertices = new Vector2[5];
                vertices[0] = new Vector2(-11.75f, 21.80f);
                vertices[1] = new Vector2(-25.35f, 6.60f);
                vertices[2] = new Vector2(-20.55f, -17.00f);
                vertices[3] = new Vector2(-4.55f, -19.80f);
                vertices[4] = new Vector2(1.45f, 11.40f);
            } else if (textureWreck == TextureRegister.particleWreckHumanSmall0Wreck1) {
                vertices = new Vector2[4];
                vertices[0] = new Vector2(7.05f, 30.20f);
                vertices[1] = new Vector2(-8.95f, 27.00f);
                vertices[2] = new Vector2(0.65f, -21.00f);
                vertices[3] = new Vector2(25.85f, -3.00f);
            } else if (textureWreck == TextureRegister.particleWreckSaimonSmall0Wreck0) {
                vertices = new Vector2[4];
                vertices[0] = new Vector2(-44.94f, 15.66f);
                vertices[1] = new Vector2(-27.98f, -10.42f);
                vertices[2] = new Vector2(11.13f, -30.42f);
                vertices[3] = new Vector2(-7.56f, 6.96f);
            } else if (textureWreck == TextureRegister.particleWreckSaimonSmall0Wreck1) {
                vertices = new Vector2[3];
                vertices[0] = new Vector2(2.88f, 20.00f);
                vertices[1] = new Vector2(-5.82f, -9.56f);
                vertices[2] = new Vector2(45.47f, -0.86f);
            } else {
                vertices = new Vector2[4];
                vertices[0] = new Vector2(7.21f, 9.16f);
                vertices[1] = new Vector2(-9.19f, 4.76f);
                vertices[2] = new Vector2(-7.99f, -10.44f);
                vertices[3] = new Vector2(8.81f, -7.24f);
            }

            for (int i = 0; i < vertices.length; i++) {
                Vector2 vertex = vertices[i];
                vertex.divide(10.0f);
            }

            Polygon p = Geometry.createPolygon(vertices);
            BodyFixture bodyFixture = new BodyFixture(p);
            bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
            bodyFixture.setFilter(new WreckFilter(this));
            body.addFixture(bodyFixture);
        } else if (light) {
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
                Vector2f pos;
                if (shipWreck) {
                    Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
                    pos = new Vector2f((float) worldPos.x, (float) worldPos.y);
                } else {
                    pos = getPosition();
                }
                Vector2f scale = getScale();
                ParticleSpawner.spawnLight(pos.x, pos.y, getScale().x * 2.0f, 1.0f, 0.8f, 0.6f, 1.0f, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnSpark(pos.x, pos.y, getScale().x);
                ParticleSpawner.spawnExplosion(pos.x, pos.y, getScale().x);
                ParticleSpawner.spawnSmallGarbage(random.nextInt(10), pos.x, pos.y, 2.0f, 5.0f + getScale().x);
                ParticleSpawner.spawnShipOst(random.nextInt(3), pos.x, pos.y, velocity.x * 0.06f, velocity.y * 0.06f, 0.25f + 0.75f * random.nextFloat());

                if (shipWreck) {
                    ParticleSpawner.spawnMediumGarbage(3, pos.x, pos.y, velocity.x * 0.1f, velocity.y * 0.1f, scale.x / 2.0f);
                    Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, pos.x, pos.y));
                }
            }

            super.setDead(isDead);
        } else {
            if (color.w > 0.01f) {
                if (shipWreck) {
                    Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
                    ParticleSpawner.spawnDamageDebris(world, random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y, 1.0f);
                    ParticleSpawner.spawnDamageWrecks(world, random.nextInt(2), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y);
                }
            }

            super.setDead(isDead);
        }
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

        float fireSpeed = 0.120f * TimeUtils.UPDATE_DELTA_TIME;
        float fireSpeed1 = 0.180f * TimeUtils.UPDATE_DELTA_TIME;
        float fireSpeed2 = 0.120F * TimeUtils.UPDATE_DELTA_TIME;
        float fireAddSpeed = 0.0300f * TimeUtils.UPDATE_DELTA_TIME;
        float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;

        if (fire && world.isRemote()) {
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

            if (changeFire) {
                if (colorFire.w > (shipWreck ? 0.7F : 0.6F)) {
                    if (shipWreck) {
                        colorFire.w -= fireSpeed - fireAddSpeed;
                        colorFire.x -= fireSpeed;
                        colorFire.y -= fireSpeed / 4.0f;
                        colorFire.z -= fireSpeed / 4.0f;
                        if (colorFire.w < 0.0f) {
                            colorFire.set(0.0f);
                        }
                    } else {
                        colorFire.w -= fireSpeed1 - fireAddSpeed;
                        colorFire.x -= fireSpeed1;
                        colorFire.y -= fireSpeed1;
                        colorFire.z -= fireSpeed1;
                        if (colorFire.w < 0.0f) {
                            colorFire.set(0.0f);
                        }
                    }
                } else {
                    changeFire = false;
                }
            } else {
                if (colorFire.w < 1.0f) {
                    if (shipWreck) {
                        colorFire.w += fireSpeed + fireAddSpeed;
                        colorFire.x += fireSpeed;
                        colorFire.y += fireSpeed / 4.0f;
                        colorFire.z += fireSpeed / 4.0f;
                    } else {
                        colorFire.w += fireSpeed1 + fireAddSpeed;
                        colorFire.x += fireSpeed1;
                        colorFire.y += fireSpeed1;
                        colorFire.z += fireSpeed1;
                    }
                } else {
                    changeFire = true;
                }
            }
        }

        if (light && world.isRemote()) {
            timerLight1 -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (timerLight1 <= 0.0f) {
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
                        timerLight += 25.0f;
                    }
                } else {
                    if (colorLight.w < color.w) {
                        colorLight.w += lightSpeed;
                        colorLight.x += lightSpeed;
                        colorLight.y += lightSpeed;
                        colorLight.z += lightSpeed;
                    } else {
                        changeLight = true;
                        timerLight += 25.0f;
                    }
                }
            }
            if (timerLight >= 100.0f) {
                timerLight1 = 200.0f + random.nextInt(200);
                timerLight = 0.0f;
            }
        }

        if (shipWreck) {
            if (world.isRemote() && lifeTime <= maxLifeTime / 2.0f) {
                if (colorFire.w > 0.0f) {
                    fire = false;
                    colorFire.w -= fireSpeed2;
                    colorFire.x -= fireSpeed2;
                    colorFire.y -= fireSpeed2;
                    colorFire.z -= fireSpeed2;
                    if (colorFire.w < 0.0f) {
                        colorFire.set(0.0f);
                    }
                }
            }

            lifeTime -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (lifeTime <= maxLifeTime / 7.0f) {
                if (world.isRemote()) {
                    if (colorLight.w > 0.0f) {
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
            } else if (lifeTime <= 0) {
                if (!world.isRemote()) {
                    MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                    setDead(true);
                }
            }
        } else {
            if (color.w <= 0.5f) {
                if (colorFire.w > 0.0f) {
                    fire = false;
                    colorFire.w -= fireSpeed2;
                    colorFire.x -= fireSpeed2;
                    colorFire.y -= fireSpeed2;
                    colorFire.z -= fireSpeed2;
                    if (colorFire.w < 0.0f) {
                        colorFire.set(0.0f);
                    }
                }

                if (color.w < 0.3f) {
                    if (colorLight.w > 0.0f) {
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
        Core.getCore().getWorld().removePhysicObject(this);
    }
}
