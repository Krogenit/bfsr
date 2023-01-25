package net.bfsr.client.particle;

import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleWreck extends Particle {
    private final int textureOffset;

    private boolean fire;
    private boolean light;
    private final boolean fireExplosion;
    private boolean changeFire;
    private boolean changeLight;
    private boolean shipWreck;

    private float explosionTimer, timerLight, timerLight1, wreckLifeTime, maxWreckLifeTime, hull;

    private TextureRegister textureWreck;
    private Texture textureFire, textureLight;

    private Vector4f colorFire, colorLight;

    private Random rand;

    private int destroyedShipId;

    public ParticleWreck(int id, int textureOffset, boolean isWreck, boolean fire, boolean fireExplosion, Vector2f pos, Vector2f vel, float angle, float angleVel, Vector2f size, float sizeVel, Vector4f color, float alphaVel) {
        super(id, pos, vel, angle, angleVel, size, sizeVel, color, alphaVel, 0.001f, false, true, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        this.textureOffset = textureOffset;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        light = isWreck;
        shipWreck = false;
        hull = 10;
        createBody1(pos);
        createAABB();
    }

    public ParticleWreck(int id, int textureOffset, Ship ship, Vector2f pos, Vector2f vel, float angle, float angleVel, Vector2f size, float sizeVel, Vector4f color, float alphaVel, float wreckLifeTime) {
        super(id, pos, vel, angle, angleVel, size, sizeVel, color, alphaVel, 0.25f, false, true, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        textureWreck = ship.getWreckTexture(textureOffset);
        this.textureOffset = textureOffset;
        fireExplosion = true;
        fire = true;
        light = true;
        shipWreck = true;
        this.wreckLifeTime = maxWreckLifeTime = wreckLifeTime;
        destroyedShipId = ship.getId();
        hull = ship.getHull().getMaxHull() / 4.0f;
        rand = world.getRand();
        createBody1(pos);
        createAABB();
    }

    public ParticleWreck(int textureOffset, boolean isWreck, boolean fire, boolean fireExplosion, Vector2f pos, Vector2f vel, float angle, float angleVel, Vector2f size, float sizeVel, Vector4f color, float alphaVel, int id) {
        super(id, getDebrisTexture(textureOffset, isWreck), pos, vel, angle, angleVel, size, sizeVel, color, alphaVel, 0.001f, false, true, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);

        this.textureOffset = textureOffset;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        light = isWreck;
        rand = world.getRand();
        hull = 10;

        if (isWreck) {
            textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckFire0.ordinal() + textureOffset]);
            textureLight = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleWreckLight0.ordinal() + textureOffset]);
//			this.timerLight = timerLight;
            colorLight = new Vector4f();
            colorLight.w = 0.0f;
            timerLight1 = 200.0f + rand.nextInt(200);
        } else if (fire) {
            textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.particleDerbisEmber1.ordinal() + textureOffset]);
        }

        if (isWreck || fire) {
            colorFire = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
//			this.timerFire = timerFire;
        }

        createBody1(pos);
        createAABB();
    }

    public ParticleWreck(int textureOffset, Ship ship, Vector2f pos, Vector2f vel, float angle, float angleVel, Vector2f size, float sizeVel, Vector4f color, float alphaVel, int id, float wreckLifeTime) {
        super(id, ship.getWreckTexture(textureOffset), pos, vel, angle, angleVel, size, sizeVel, color, alphaVel, 0.25f, false, true, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);

        rand = world.getRand();
        textureWreck = ship.getWreckTexture(textureOffset);
        this.textureOffset = textureOffset;
        textureFire = TextureLoader.getTexture(ship.getWreckFireTexture(textureOffset));
        textureLight = TextureLoader.getTexture(ship.getWreckLightTexture(textureOffset));
        fireExplosion = false;
        fire = true;
        light = true;
        colorFire = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        colorFire.w = 0.0f;
        colorLight = new Vector4f();
        colorLight.w = 0.0f;
        shipWreck = true;
//		this.timerFire = timerFire;
//		this.timerLight = timerLight;
        rand = world.getRand();
        timerLight1 = 200.0f + rand.nextInt(200);
        this.wreckLifeTime = maxWreckLifeTime = wreckLifeTime;
        hull = ship.getHull().getMaxHull() / 4.0f;
        createBody1(pos);
        createAABB();
    }

    @Override
    protected void addParticle() {
        ParticleRenderer.getInstance().addParticle(this);
    }

    private void createBody1(Vector2f pos) {
        body = new Body();
        createFixtures();
        body.translate(pos.x, pos.y);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rotate);
        body.setAngularVelocity(rotationSpeed);
        if (shipWreck) {
            body.setLinearDamping(0.2f);
            body.setAngularDamping(0.025f);
        } else {
            body.setLinearDamping(0.05f);
            body.setAngularDamping(0.005f);
        }

        world.addDynamicParticle(this);
    }

    @Override
    protected void createBody(Vector2f pos) {

    }

    @Override
    protected void createFixtures() {
        if (shipWreck) {
            Vector2[] vertecies;
            switch (textureWreck) {
                case particleWreckEngiSmall0Wreck0:
                    vertecies = new Vector2[6];
                    vertecies[0] = new Vector2(-16.80f, 20.20f);
                    vertecies[1] = new Vector2(-32.80f, 4.60f);
                    vertecies[2] = new Vector2(-32.00f, -11.40f);
                    vertecies[3] = new Vector2(-15.60f, -20.20f);
                    vertecies[4] = new Vector2(-0.40f, -19.00f);
                    vertecies[5] = new Vector2(-4.00f, 16.60f);
                    break;
                case particleWreckEngiSmall0Wreck1:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(14.00f, 13.40f);
                    vertecies[1] = new Vector2(-6.80f, -5.00f);
                    vertecies[2] = new Vector2(7.60f, -21.00f);
                    vertecies[3] = new Vector2(33.60f, -7.00f);
                    vertecies[4] = new Vector2(34.40f, 2.60f);
                    break;
                case particleWreckHumanSmall0Wreck0:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(-11.75f, 21.80f);
                    vertecies[1] = new Vector2(-25.35f, 6.60f);
                    vertecies[2] = new Vector2(-20.55f, -17.00f);
                    vertecies[3] = new Vector2(-4.55f, -19.80f);
                    vertecies[4] = new Vector2(1.45f, 11.40f);
                    break;
                case particleWreckHumanSmall0Wreck1:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(7.05f, 30.20f);
                    vertecies[1] = new Vector2(-8.95f, 27.00f);
                    vertecies[2] = new Vector2(0.65f, -21.00f);
                    vertecies[3] = new Vector2(25.85f, -3.00f);
                    break;
                case particleWreckSaimonSmall0Wreck0:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(-44.94f, 15.66f);
                    vertecies[1] = new Vector2(-27.98f, -10.42f);
                    vertecies[2] = new Vector2(11.13f, -30.42f);
                    vertecies[3] = new Vector2(-7.56f, 6.96f);
                    break;
                case particleWreckSaimonSmall0Wreck1:
                    vertecies = new Vector2[3];
                    vertecies[0] = new Vector2(2.88f, 20.00f);
                    vertecies[1] = new Vector2(-5.82f, -9.56f);
                    vertecies[2] = new Vector2(45.47f, -0.86f);
                    break;
                default:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(7.21f, 9.16f);
                    vertecies[1] = new Vector2(-9.19f, 4.76f);
                    vertecies[2] = new Vector2(-7.99f, -10.44f);
                    vertecies[3] = new Vector2(8.81f, -7.24f);
                    break;
            }

            for (int i = 0; i < vertecies.length; i++) {
                Vector2 vertex = vertecies[i];
                vertex.divide(10.0f);
            }

            Polygon p = Geometry.createPolygon(vertecies);
            BodyFixture bodyFixture = new BodyFixture(p);
            bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
            bodyFixture.setFilter(new WreckFilter(this));
            body.addFixture(bodyFixture);
        } else if (light) {
            Vector2[] vertecies = null;
            switch (textureOffset) {
                case 0:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(7.21f, 9.16f);
                    vertecies[1] = new Vector2(-9.19f, 4.76f);
                    vertecies[2] = new Vector2(-7.99f, -10.44f);
                    vertecies[3] = new Vector2(8.81f, -7.24f);
                    break;
                case 1:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(-0.79f, 11.56f);
                    vertecies[1] = new Vector2(-7.99f, -0.44f);
                    vertecies[2] = new Vector2(3.61f, -10.84f);
                    vertecies[3] = new Vector2(6.81f, 1.56f);
                    break;
                case 2:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(-5.16f, 7.97f);
                    vertecies[1] = new Vector2(-11.96f, -0.03f);
                    vertecies[2] = new Vector2(-5.96f, -9.23f);
                    vertecies[3] = new Vector2(7.24f, -11.23f);
                    vertecies[4] = new Vector2(8.84f, 1.97f);
                    break;
            }

            for (Vector2 vertex : vertecies) {
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
            switch (textureOffset) {
                case 0:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(3.40f, 10.93f);
                    vertecies[1] = new Vector2(-11.00f, -10.27f);
                    vertecies[2] = new Vector2(4.60f, -9.07f);
                    vertecies[3] = new Vector2(12.60f, 4.93f);
                    break;
                case 1:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(7.94f, 5.84f);
                    vertecies[1] = new Vector2(-12.26f, -4.51f);
                    vertecies[2] = new Vector2(-9.56f, -7.29f);
                    vertecies[3] = new Vector2(12.15f, -0.43f);
                    break;
                case 2:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(-3.61f, 9.74f);
                    vertecies[1] = new Vector2(-11.21f, -6.66f);
                    vertecies[2] = new Vector2(-2.51f, -13.46f);
                    vertecies[3] = new Vector2(11.89f, -7.86f);
                    vertecies[4] = new Vector2(8.29f, 9.34f);
                    break;
                case 3:
                    vertecies = new Vector2[4];
                    vertecies[0] = new Vector2(-6.88f, 8.32f);
                    vertecies[1] = new Vector2(-8.08f, -2.88f);
                    vertecies[2] = new Vector2(-0.48f, -10.48f);
                    vertecies[3] = new Vector2(6.72f, 3.92f);
                    break;
                case 4:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(-2.48f, 12.80f);
                    vertecies[1] = new Vector2(-12.88f, 1.20f);
                    vertecies[2] = new Vector2(-6.08f, -10.00f);
                    vertecies[3] = new Vector2(12.72f, -8.00f);
                    vertecies[4] = new Vector2(4.32f, 12.00f);
                    break;
                case 5:
                    vertecies = new Vector2[5];
                    vertecies[0] = new Vector2(-6.88f, 11.20f);
                    vertecies[1] = new Vector2(-12.48f, -6.00f);
                    vertecies[2] = new Vector2(2.72f, -13.60f);
                    vertecies[3] = new Vector2(13.52f, -4.80f);
                    vertecies[4] = new Vector2(6.72f, 12.40f);
                    break;
            }

            for (Vector2 vertex : vertecies) {
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
                ParticleSpawner.spawnLight(pos, getScale().x * 2.0f, new Vector4f(1.0f, 0.8f, 0.6f, 1.0f), EnumParticlePositionType.Default);
                ParticleSpawner.spawnSpark(pos, getScale().x);
                ParticleSpawner.spawnExplosion(pos, getScale().x);
                Vector2f position = getPosition();
                ParticleSpawner.spawnSmallGarbage(rand.nextInt(10), position.x, position.y, 2.0f, 5.0f + getScale().x);
                ParticleSpawner.spawnShipOst(rand.nextInt(3), position, new Vector2f(velocity).mul(0.06f), 0.25f + 0.75f * rand.nextFloat());

                if (shipWreck) {
                    ParticleSpawner.spawnMediumGarbage(3, pos, new Vector2f(velocity).mul(0.1f), scale.x / 2.0f);
                    Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, pos));
                }
            }

            super.setDead(isDead);
        } else {
            if (color.w > 0.01f) {
                Vector2f pos;
                if (shipWreck) {
                    Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
                    pos = new Vector2f((float) worldPos.x, (float) worldPos.y);
                    ParticleSpawner.spawnDamageDerbis(world, rand.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y, 1.0f);
                    ParticleSpawner.spawnDamageWrecks(world, rand.nextInt(2), pos, velocity);
                }
            }

            super.setDead(isDead);
        }
    }

    @Override
    public void update() {
        if (world.isRemote()) {
            aliveTimer += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (aliveTimer > 120) {
                setDead(true);
                aliveTimer = 0;
            }
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        if (!canCollide) {
            position.x += velocity.x * TimeUtils.UPDATE_DELTA_TIME;
            position.y += velocity.y * TimeUtils.UPDATE_DELTA_TIME;
            rotate += rotationSpeed * TimeUtils.UPDATE_DELTA_TIME;

            if (!zeroVelocity) {
                velocity.x *= 0.999f;
                velocity.y *= 0.999f;
            }
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
                    ParticleSpawner.spawnExplosion(new Vector2f(getPosition()), size / 10.0f);
                    ParticleSpawner.spawnDamageSmoke(new Vector2f(getPosition()), size + 1.0f);
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
                    } else {
                        colorFire.w -= fireSpeed1 - fireAddSpeed;
                        colorFire.x -= fireSpeed1;
                        colorFire.y -= fireSpeed1;
                        colorFire.z -= fireSpeed1;
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
                timerLight1 = 200.0f + rand.nextInt(200);
                timerLight = 0.0f;
            }
        }

        if (shipWreck) {
            if (world.isRemote() && wreckLifeTime <= maxWreckLifeTime / 2.0f) {
                if (colorFire.w > 0.0f) {
                    fire = false;
                    colorFire.w -= fireSpeed2;
                    colorFire.x -= fireSpeed2;
                    colorFire.y -= fireSpeed2;
                    colorFire.z -= fireSpeed2;
                }
            }

            wreckLifeTime -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (wreckLifeTime <= maxWreckLifeTime / 7.0f) {
                if (world.isRemote() && colorLight.w > 0.0f) {
                    light = false;
                    colorLight.w -= lightSpeed;
                    colorLight.x -= lightSpeed;
                    colorLight.y -= lightSpeed;
                    colorLight.z -= lightSpeed;
                }

//				if(color.w < 0.2f) {
//					color.w -= alphaVelocity * 3f * dt;
//					if(color.w <= 0f) {
//						color.w = 0f;
//						if(!world.isRemote()) {
//							MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new SRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
//							setDead(true);
//						}
//					}
//						
//				} else {
//					color.w -= alphaVelocity * dt;
//				}
            } else if (wreckLifeTime <= 0) {
                if (!world.isRemote()) {
                    MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                    setDead(true);
                }
            }
        } else {
            if (color.w <= 0.5f) {
                if (colorFire != null && colorFire.w > 0.0f) {
                    fire = false;
                    colorFire.w -= fireSpeed2;
                    colorFire.x -= fireSpeed2;
                    colorFire.y -= fireSpeed2;
                    colorFire.z -= fireSpeed2;
                }

                if (color.w < 0.3f) {
                    if (colorLight != null && colorLight.w > 0.0f) {
                        light = false;
                        colorLight.w -= lightSpeed;
                        colorLight.x -= lightSpeed;
                        colorLight.y -= lightSpeed;
                        colorLight.z -= lightSpeed;
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
            }
        }
    }

    public void damage(float damage) {
        hull -= damage;
        if (!world.isRemote() && hull <= 0) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            setDead(true);
        }
    }

    void renderEffects(BaseShader shader) {
        if (colorFire != null && colorFire.w > 0) {
            shader.setColor(colorFire.x, colorFire.y, colorFire.z, colorFire.w);
            OpenGLHelper.bindTexture(textureFire.getId());
            shader.setModelMatrix(Transformation.getModelViewMatrix(this).get(ShaderProgram.MATRIX_BUFFER));
            Renderer.centeredQuad.renderIndexed();
        }

        if (colorLight != null && colorLight.w > 0) {
            shader.setColor(colorLight.x, colorLight.y, colorLight.z, colorLight.w);
            OpenGLHelper.bindTexture(textureLight.getId());
            shader.setModelMatrix(Transformation.getModelViewMatrix(this).get(ShaderProgram.MATRIX_BUFFER));
            Renderer.centeredQuad.renderIndexed();
        }
    }

    private static TextureRegister getDebrisTexture(int textureOffset, boolean isWreck) {
        if (isWreck) {
            return TextureRegister.values()[TextureRegister.particleWreck0.ordinal() + textureOffset];
        } else {
            return TextureRegister.values()[TextureRegister.particleDerbis1.ordinal() + textureOffset];
        }
    }

    public boolean isFire() {
        return fire;
    }

    public boolean isLight() {
        return light;
    }

    public boolean isShipWreck() {
        return shipWreck;
    }

    public boolean isFireExplosion() {
        return fireExplosion;
    }

    public float getWreckLifeTime() {
        return wreckLifeTime;
    }

    public float getMaxWreckLifeTime() {
        return maxWreckLifeTime;
    }

    public int getDestroyedShipId() {
        return destroyedShipId;
    }

    public int getTextureOffset() {
        return textureOffset;
    }

    public float getHull() {
        return hull;
    }

    public void setHull(float hull) {
        this.hull = hull;
    }
}
