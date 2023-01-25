package net.bfsr.entity.bullet;

import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.client.render.OpenGLHelper;
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
import net.bfsr.network.packet.server.PacketSpawnBullet;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Bullet extends CollisionObject {
    protected final Ship ship;
    private final float bulletSpeed;
    private float alphaReducer;
    private BulletDamage damage;
    private float energy;
    private Object previousAObject;

    public Bullet(WorldClient world, int id, float bulletSpeed, float radRot, Vector2f pos, Vector2f scale, Ship ship, TextureRegister texture, Vector4f color, float alphaReducer, BulletDamage damage) {
        super(world, id, texture, pos, scale);
        this.color = color;
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(radRot, pos);
        world.addBullet(this);
    }

    public Bullet(WorldServer world, int id, float bulletSpeed, float radRot, Vector2f pos, Vector2f scale, Ship ship, Vector4f color, float alphaReducer, BulletDamage damage) {
        super(world, id, pos, scale);
        this.color = color;
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(radRot, pos);
        world.addBullet(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnBullet(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    private void setBulletVelocityAndStartTransform(float radRot, Vector2f pos) {
        double x1 = Math.cos(radRot);
        double y1 = Math.sin(radRot);
        Vector2 velocity = new Vector2(x1 * bulletSpeed, y1 * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(radRot);
        body.getTransform().setTranslation(pos.x + velocity.x / 500.0f, pos.y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);
    }

    @Override
    public void update() {
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
                    if (damageShip(ship)) {
                        if (world.isRemote()) Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damageNoShield, getPosition()));
                        //Hull damage
                        destroyBullet(ship, contact, normal);
                        setDead(true);
                    } else {
                        if (world.isRemote()) Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damage, getPosition()));
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
            if (destroyer != null) {
                if (destroyer instanceof Ship s) {
                    Shield shield = s.getShield();
                    if (shield == null || shield.getShield() <= 0) {
                        Hull hull = s.getHull();
                        Vector2 pos1 = contact.getPoint();
                        Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                        Vector2f velocity = new Vector2f(destroyer.getVelocity()).mul(0.005f);
                        Random rand = world.getRand();
                        ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, new Vector4f(color));
                        if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(2) == 0) {
                            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                            ParticleSpawner.spawnShipOst(1, pos, new Vector2f(velocity).add(angletovel), 0.5f);
                        }
                        Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f));
                        ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                    }

                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, new Vector4f(color));
                } else if (destroyer instanceof Bullet) {
                    ParticleSpawner.spawnLight(getPosition(), getScale().x * 5.0f, 7.0f * 6.0f, new Vector4f(color.x, color.y, color.z, 0.5f), 0.25f * 60.0f, true, EnumParticlePositionType.Default);
                } else if (destroyer instanceof ParticleWreck) {
                    Vector2 pos1 = contact.getPoint();
                    Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, new Vector4f(color));
                    Random rand = world.getRand();
                    if (rand.nextInt(4) == 0) {
                        Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                        ParticleSpawner.spawnShipOst(1, pos, new Vector2f(velocity).add(angletovel), 0.5f);
                    }
                    Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f));
                    ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                }
            } else {
                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, new Vector4f(color));
            }
            ParticleSpawner.spawnLight(getPosition(), getScale().x * 3.0f, 3.0f * 6.0f, new Vector4f(color.x, color.y, color.z, 0.4f), 0.5f * 60.0f, true, EnumParticlePositionType.Default);
        } else {
            if (destroyer != null) {
                if (destroyer instanceof Ship s) {
                    Shield shield = s.getShield();
                    if (shield == null || shield.getShield() <= 0) {
                        Hull hull = s.getHull();
                        Vector2 pos1 = contact.getPoint();
                        Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                        Vector2f velocity = new Vector2f(destroyer.getVelocity()).mul(0.005f);
                        Random rand = world.getRand();
                        if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
                            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                            ParticleSpawner.spawnDamageDerbis(world, rand.nextInt(2), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 0.75f);
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
    public void render(BaseShader shader) {
        OpenGLHelper.alphaGreater(0.01f);
        super.render(shader);
    }

    public BulletDamage getDamage() {
        return damage;
    }

    public void setDamage(BulletDamage damage) {
        this.damage = damage;
    }

    public void setAlphaReducer(float alphaReducer) {
        this.alphaReducer = alphaReducer;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public Ship getOwnerShip() {
        return ship;
    }
}
