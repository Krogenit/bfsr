package net.bfsr.entity.bullet;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.Shield;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.server.PacketSpawnBullet;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.Random;

public class Bullet extends CollisionObject {
    protected final Ship ship;
    private final float bulletSpeed;
    private final float alphaReducer;
    private BulletDamage damage;
    private float energy;
    private Object previousAObject;

    public Bullet(WorldClient world, int id, float bulletSpeed, float radRot, float x, float y, float scaleX, float scaleY, Ship ship, TextureRegister texture,
                  float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, texture, x, y, scaleX, scaleY, r, g, b, a);
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(radRot, x, y);
        world.addBullet(this);
    }

    public Bullet(WorldServer world, int id, float bulletSpeed, float radRot, float x, float y, float scaleX, float scaleY, Ship ship, float r, float g, float b, float a,
                  float alphaReducer, BulletDamage damage) {
        super(world, id, x, y, scaleX, scaleY, r, g, b, a);
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(radRot, x, y);
        world.addBullet(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnBullet(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    private void setBulletVelocityAndStartTransform(float radRot, float x, float y) {
        double x1 = Math.cos(radRot);
        double y1 = Math.sin(radRot);
        velocity.set(x1 * bulletSpeed, y1 * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(radRot);
        body.getTransform().setTranslation(x + velocity.x / 500.0f, y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
    }

    @Override
    public void update() {
        lastPosition.set(position);
        super.update();

        color.w -= alphaReducer * TimeUtils.UPDATE_DELTA_TIME;

        if (color.w <= 0) {
            setDead(true);
        }

        if (world.isRemote()) {
            aliveTimer = 0;
        }
    }

    public void postPhysicsUpdate() {
        Vector2 velocity = body.getLinearVelocity();
        double mDx = velocity.x;
        double mDy = velocity.y;

        double rotateToVector = Math.atan2(mDx, -mDy);
        body.getTransform().setRotation(rotateToVector + Math.PI / 2.0);
    }

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    Vector2f position = getPosition();
                    if (damageShip(ship)) {
                        if (world.isRemote()) Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damageNoShield, position.x, position.y));
                        //Hull damage
                        destroyBullet(ship, contact, normal);
                        setDead(true);
                    } else {
                        if (world.isRemote()) Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damage, position.x, position.y));
                        //Shield reflection
                        destroyBullet(ship, contact, normal);
                        damage(this);
                    }
                } else if (previousAObject != null && previousAObject != ship && this.ship == ship) {
                    previousAObject = ship;
                    //We can damage ship after some collission with other object
                    destroyBullet(ship, contact, normal);
                }
            } else if (userData instanceof Bullet bullet) {
                //Bullet vs bullet
                bullet.damage(this);
                previousAObject = bullet;

                if (bullet.isDead()) {
                    bullet.destroyBullet(this, contact, normal);
                }
            } else if (userData instanceof ParticleWreck wreck) {
                wreck.damage(damage.bulletDamageHull);
                destroyBullet(wreck, contact, normal);
            }
        }
    }

    private void damage(Bullet bullet) {
        float damage = bullet.damage.getAverageDamage();
        damage /= 3.0f;

        this.damage.bulletDamageArmor -= damage;
        this.damage.bulletDamageHull -= damage;
        this.damage.bulletDamageShield -= damage;

        if (this.damage.bulletDamageArmor < 0) setDead(true);
        else if (this.damage.bulletDamageHull < 0) setDead(true);
        else if (this.damage.bulletDamageShield < 0) setDead(true);

        if (bullet != this) {
            energy -= damage;

            if (energy <= 0) {
                setDead(true);
            }
        }
    }

    private void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        if (world.isRemote()) {
            Vector2f position = getPosition();
            if (destroyer != null) {
                if (destroyer instanceof Ship s) {
                    Shield shield = s.getShield();
                    if (shield == null || shield.getShield() <= 0) {
                        Hull hull = s.getHull();
                        Vector2 pos1 = contact.getPoint();
                        float velocityX = destroyer.getVelocity().x * 0.005f;
                        float velocityY = destroyer.getVelocity().y * 0.005f;
                        Random rand = world.getRand();
                        ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                        if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(2) == 0) {
                            RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, angleToVelocity);
                            ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.5f);
                        }
                        Vector2f angleToVelocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f));
                        ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y,
                                2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                    }

                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                } else if (destroyer instanceof Bullet) {
                    ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 5.0f, 7.0f * 6.0f, color.x, color.y, color.z, 0.5f, 0.25f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                } else if (destroyer instanceof ParticleWreck) {
                    Vector2 pos1 = contact.getPoint();
                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                    Random rand = world.getRand();
                    if (rand.nextInt(4) == 0) {
                        RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, angleToVelocity);
                        ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocity.x + angleToVelocity.x, velocity.y + angleToVelocity.y, 0.5f);
                    }
                    RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f), angleToVelocity);
                    ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocity.x + angleToVelocity.x, velocity.y + angleToVelocity.y,
                            2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                }
            } else {
                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
            }
            ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 3.0f, 3.0f * 6.0f, color.x, color.y, color.z, 0.4f, 0.5f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
        } else {
            if (destroyer != null) {
                if (destroyer instanceof Ship s) {
                    Shield shield = s.getShield();
                    if (shield == null || shield.getShield() <= 0) {
                        Hull hull = s.getHull();
                        Vector2 pos1 = contact.getPoint();
                        float velocityX = destroyer.getVelocity().x * 0.005f;
                        float velocityY = destroyer.getVelocity().y * 0.005f;
                        Random rand = world.getRand();
                        if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
                            RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, angleToVelocity);
                            ParticleSpawner.spawnDamageDebris(world, rand.nextInt(2), (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f);
                        }
                    }
                }
            }
        }
    }

    public boolean canDamageShip(Ship ship) {
        return this.ship != ship && previousAObject != ship;
    }

    private boolean damageShip(Ship ship) {
        return ship.attackShip(damage, ship, getPosition(), ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }

    @Override
    public void render(BaseShader shader, float interpolation) {
        float size = 6.0f;
        Vector2f pos = getPosition();
        InstancedRenderer.INSTANCE.addToRenderPipeLine(Transformation.getDefaultModelMatrix(lastPosition.x, lastPosition.y, pos.x, pos.y, getRotation(), size, size, interpolation),
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, color.w / 4.0f, TextureLoader.getTexture(TextureRegister.particleLight));
        InstancedRenderer.INSTANCE.addToRenderPipeLine(this, interpolation);
    }

    public BulletDamage getDamage() {
        return damage;
    }

    public void setDamage(BulletDamage damage) {
        this.damage = damage;
    }

    public Ship getOwnerShip() {
        return ship;
    }
}
