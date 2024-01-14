package net.bfsr.entity.bullet;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.RigidBodyPostPhysicsUpdateEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.physics.filter.BulletFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.world.ContactCollisionData;

import java.util.function.Consumer;

public class Bullet extends RigidBody<GunData> {
    @Getter
    protected final RigidBody<?> owner;
    private final float bulletSpeed;
    private final BulletDamage damage;
    private final Polygon polygon;
    private Object previousAObject;

    public Bullet(float x, float y, float sin, float cos, GunData gunData, RigidBody<?> owner, BulletDamage damage) {
        super(x, y, sin, cos, gunData.getBulletSizeX(), gunData.getBulletSizeY(), gunData, GunRegistry.INSTANCE.getId());
        this.owner = owner;
        this.maxLifeTime = gunData.getBulletLifeTimeInTicks();
        this.bulletSpeed = gunData.getBulletSpeed();
        this.damage = damage;
        this.health = damage.getAverage();
        this.polygon = gunData.getBulletPolygon();
        this.velocity.set(cos * bulletSpeed, sin * bulletSpeed);
    }

    @Override
    protected void initBody() {
        BodyFixture bodyFixture = new BodyFixture(polygon);
        bodyFixture.setFilter(new BulletFilter(this));
        bodyFixture.setSensor(true);

        body.setBullet(true);
        body.addFixture(bodyFixture);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime++;

        if (lifeTime >= maxLifeTime) {
            setDead();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();

        eventBus.publish(new RigidBodyPostPhysicsUpdateEvent(this));
    }

    @Setter
    private Consumer<Bullet> onAddedToWorldConsumer = bullet -> {};

    @Override
    public void onAddedToWorld() {
        onAddedToWorldConsumer.accept(this);
    }

    @Override
    public void collision(Body body, BodyFixture fixture, float contactX, float contactY, float normalX, float normalY,
                          ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);

        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    ship.damage(damage, ship, contactX, contactY, ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f,
                            fixture, () -> {
                                //Shield
                                damage(this);
                                reflect(normalX, normalY);
                                eventBus.publish(
                                        new BulletDamageShipShieldEvent(this, ship, contactX, contactY, normalX, normalY));
                            }, () -> {
                                //Armor
                                setDead();
                                eventBus.publish(
                                        new BulletDamageShipArmorEvent(this, ship, contactX, contactY, normalX, normalY));
                            },
                            () -> {
                                //Hull
                                setDead();
                                eventBus.publish(new BulletDamageShipHullEvent(this, ship, contactX, contactY, normalX, normalY));
                            });
                }
            } else if (userData instanceof Bullet bullet) {
                bullet.setDead();
            } else if (userData instanceof Wreck wreck) {
                wreck.damage(damage.getHull(), contactX, contactY, normalX, normalY);
                setDead();
            } else if (userData instanceof ShipWreck wreck) {
                wreck.damage(this, contactX, contactY, normalX, normalY);
                setDead();
            } else if (userData instanceof RigidBody<?> rigidBody) {
                rigidBody.damage(damage.getHull(), contactX, contactY, normalX, normalY);
                setDead();
            }
        }
    }

    private void reflect(float normalX, float normalY) {
        float dot = velocity.x * normalX + velocity.y * normalY;
        setVelocity(velocity.x - 2 * dot * normalX, velocity.y - 2 * dot * normalY);
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        setRotation(LUT.sin(rotateToVector), LUT.cos(rotateToVector));
    }

    private void damage(Bullet bullet) {
        if (!world.isServer()) return;

        float damage = bullet.damage.getAverage();
        damage /= 3.0f;

        this.damage.reduceBulletDamageArmor(damage);
        this.damage.reduceBulletDamageHull(damage);
        this.damage.reduceBulletDamageShield(damage);

        if (this.damage.getArmor() < 0) setDead();
        else if (this.damage.getHull() < 0) setDead();
        else if (this.damage.getShield() < 0) setDead();

        if (bullet != this) {
            health -= damage;

            if (health <= 0) {
                setDead();
            }
        }
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new BulletSpawnData(this);
    }

    @Override
    protected void onDataAdded() {}

    private boolean canDamageShip(Ship ship) {
        return this.owner != ship && previousAObject != ship || previousAObject != null && previousAObject != ship;
    }

    @Override
    public boolean canCollideWith(GameObject gameObject) {
        return owner != gameObject && previousAObject != gameObject;
    }
}