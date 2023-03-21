package net.bfsr.server.entity.bullet;

import lombok.Getter;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.effect.ParticleEffect;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.wreck.ShipWreckDamagable;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.server.effect.PacketSpawnParticleEffect;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.network.packet.server.entity.bullet.PacketSpawnBullet;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;

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
        init();
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
    }

    @Override
    public void collision(Body body, float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    if (damageShip(ship, contactX, contactY)) {
                        //Hull damage
                        destroyBullet(ship, contactX, contactY);
                        setDead();
                    } else {
                        //Shield reflection
                        destroyBullet(ship, contactX, contactY);
                        damage(this);
                        reflect(normalX, normalY);
                    }

                    MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnParticleEffect(ParticleEffect.SMALL_BULLET_DAMAGE_TO_SHIP,
                            this, ship, contactX, contactY, normalX, normalY), getX(), getY(), WorldServer.PACKET_UPDATE_DISTANCE);
                } else if (previousAObject != null && previousAObject != ship && this.ship == ship) {
                    previousAObject = ship;
                    //We can damage ship after some collission with other object
                    destroyBullet(ship, contactX, contactY);
                    reflect(normalX, normalY);
                }
            } else if (userData instanceof Bullet bullet) {
                //Bullet vs bullet
                bullet.damage(this);
                previousAObject = bullet;

                if (bullet.isDead()) {
                    bullet.destroyBullet(this, contactX, contactY);
                } else {
                    reflect(normalX, normalY);
                }
            } else if (userData instanceof Wreck wreck) {
                wreck.damage(damage.getBulletDamageHull());
            } else if (userData instanceof ShipWreckDamagable shipWreckDamagable) {
                shipWreckDamagable.attackFromBullet(this, contactX, contactY, normalX, normalY);
                setDead();
            }
        }
    }

    private void reflect(float normalX, float normalY) {
        Vector2 velocity = body.getLinearVelocity();
        double dot = velocity.dot(normalX, normalY);
        velocity.x = velocity.x - 2 * dot * normalX;
        velocity.y = velocity.y - 2 * dot * normalY;
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        sin = LUT.sin(rotateToVector);
        cos = LUT.cos(rotateToVector);
        body.getTransform().setRotation(sin, cos);
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketObjectPosition(this), position.x, position.y, WorldServer.PACKET_UPDATE_DISTANCE);
    }

    private void damage(Bullet bullet) {
        float damage = bullet.damage.getAverageDamage();
        damage /= 3.0f;

        this.damage.reduceBulletDamageArmor(damage);
        this.damage.reduceBulletDamageHull(damage);
        this.damage.reduceBulletDamageShield(damage);

        if (this.damage.getBulletDamageArmor() < 0) setDead();
        else if (this.damage.getBulletDamageHull() < 0) setDead();
        else if (this.damage.getBulletDamageShield() < 0) setDead();

        if (bullet != this) {
            energy -= damage;

            if (energy <= 0) {
                setDead();
            }
        }
    }

    private void destroyBullet(CollisionObject destroyer, float contactX, float contactY) {
        if (destroyer != null) {
            if (destroyer instanceof Ship s) {
                ShieldCommon shield = s.getShield();
                if (shield == null || shield.getShield() <= 0) {
                    Hull hull = s.getHull();
                    float velocityX = destroyer.getVelocity().x * 0.005f;
                    float velocityY = destroyer.getVelocity().y * 0.005f;
                    Random rand = world.getRand();
                    if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
                        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, CollisionObjectUtils.ANGLE_TO_VELOCITY);
                        WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), contactX, contactY,
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

    private boolean damageShip(Ship ship, float contactX, float contactY) {
        return ship.attackShip(damage, ship, contactX, contactY, ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }

    @Override
    public void setDead() {
        super.setDead();
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketRemoveObject(this), position.x, position.y, WorldServer.PACKET_UPDATE_DISTANCE);
    }
}