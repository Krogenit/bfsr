package net.bfsr.server.entity.bullet;

import lombok.Getter;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.server.PacketSpawnBullet;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;

import java.util.Random;

public abstract class Bullet extends CollisionObject {
    @Getter
    protected final Ship ship;
    private final float bulletSpeed;
    private final float aliveTimeReducer;
    @Getter
    private final BulletDamage damage;
    private float energy;
    private Object previousAObject;

    protected Bullet(WorldServer world, int id, float bulletSpeed, float x, float y, float scaleX, float scaleY, Ship ship,
                     float a, float aliveTimeReducer, BulletDamage damage) {
        super(world, id, x, y, ship.getSin(), ship.getCos(), scaleX, scaleY);
        this.aliveTimeReducer = aliveTimeReducer;
        this.lifeTime = a;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(x, y);
        world.addBullet(this);
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnBullet(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    private void setBulletVelocityAndStartTransform(float x, float y) {
        velocity.set(cos * bulletSpeed, sin * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(sin, cos);
        body.getTransform().setTranslation(x + velocity.x / 500.0f, y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
    }

    @Override
    public void update() {
        super.update();

        lifeTime -= aliveTimeReducer * TimeUtils.UPDATE_DELTA_TIME;

        if (lifeTime <= 0) {
            setDead(true);
        }
    }

    @Override
    public void postPhysicsUpdate() {
        Vector2 velocity = body.getLinearVelocity();
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        sin = LUT.sin(rotateToVector);
        cos = LUT.cos(rotateToVector);
        body.getTransform().setRotation(sin, cos);
        updateWorldAABB();
    }

    @Override
    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    if (damageShip(ship)) {
                        //Hull damage
                        destroyBullet(ship, contact, normal);
                        setDead(true);
                    } else {
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
            } else if (userData instanceof Wreck wreck) {
                wreck.damage(damage.getBulletDamageHull());
                destroyBullet(wreck, contact, normal);
            }
        }
    }

    private void damage(Bullet bullet) {
        float damage = bullet.damage.getAverageDamage();
        damage /= 3.0f;

        this.damage.reduceBulletDamageArmor(damage);
        this.damage.reduceBulletDamageHull(damage);
        this.damage.reduceBulletDamageShield(damage);

        if (this.damage.getBulletDamageArmor() < 0) setDead(true);
        else if (this.damage.getBulletDamageHull() < 0) setDead(true);
        else if (this.damage.getBulletDamageShield() < 0) setDead(true);

        if (bullet != this) {
            energy -= damage;

            if (energy <= 0) {
                setDead(true);
            }
        }
    }

    private void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        if (destroyer != null) {
            if (destroyer instanceof Ship s) {
                ShieldCommon shield = s.getShield();
                if (shield == null || shield.getShield() <= 0) {
                    Hull hull = s.getHull();
                    Vector2 pos1 = contact.getPoint();
                    float velocityX = destroyer.getVelocity().x * 0.005f;
                    float velocityY = destroyer.getVelocity().y * 0.005f;
                    Random rand = world.getRand();
                    if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
                        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, CollisionObjectUtils.ANGLE_TO_VELOCITY);
                        WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), (float) pos1.x, (float) pos1.y,
                                velocityX + CollisionObjectUtils.ANGLE_TO_VELOCITY.x, velocityY + CollisionObjectUtils.ANGLE_TO_VELOCITY.y, 0.75f);
                    }
                }
            }
        }
    }

    public boolean canDamageShip(Ship ship) {
        return this.ship != ship && previousAObject != ship;
    }

    @Override
    public boolean canCollideWith(GameObject gameObject) {
        return ship != gameObject && previousAObject != gameObject;
    }

    private boolean damageShip(Ship ship) {
        return ship.attackShip(damage, ship, getPosition(), ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }
}
