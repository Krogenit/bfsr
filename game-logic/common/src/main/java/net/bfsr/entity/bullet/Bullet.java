package net.bfsr.entity.bullet;

import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.bfsr.event.entity.bullet.BulletReflectEvent;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.physics.filter.BulletFilter;
import net.bfsr.util.SyncUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

public class Bullet extends RigidBody<GunData> {
    @Getter
    protected final Ship ship;
    private final float bulletSpeed;
    private final BulletDamage damage;
    private final Polygon polygon;
    private Object previousAObject;
    @Getter
    private final float startLifeTime;

    public Bullet(float x, float y, float sin, float cos, GunData gunData, Ship ship, BulletDamage damage) {
        super(x, y, sin, cos, gunData.getBulletSizeX(), gunData.getBulletSizeY(), gunData, GunRegistry.INSTANCE.getId());
        this.ship = ship;
        this.lifeTime = gunData.getBulletLifeTimeInTicks();
        this.startLifeTime = lifeTime;
        this.bulletSpeed = gunData.getBulletSpeed();
        this.damage = damage;
        this.health = damage.getAverage();
        this.polygon = gunData.getBulletPolygon();
        this.velocity.set(cos * bulletSpeed, sin * bulletSpeed);
    }

    @Override
    public void init(World world, int id) {
        super.init(world, id);
        this.eventBus = world.getEventBus();

        if (SideUtils.IS_SERVER && this.world.isServer()) {
            body.getTransform().setTranslation(position.x + velocity.x / 500.0f,
                    position.y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
        }
    }

    @Override
    protected void initBody() {
        BodyFixture bodyFixture = new BodyFixture(polygon);
        bodyFixture.setFilter(new BulletFilter(this));
        bodyFixture.setSensor(true);

        if (SideUtils.IS_SERVER && world.isServer()) {
            body.setBullet(true);
        }

        body.addFixture(bodyFixture);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    @Override
    public void update() {
        lifeTime -= 1;

        if (lifeTime <= 0) {
            setDead();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f position, float sin, float cos, Vector2f velocity,
                                               float angularVelocity) {
        setRotation(sin, cos);
        SyncUtils.updatePos(this, position);
        body.setLinearVelocity(velocity.x, velocity.y);
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
        eventBus.publish(new BulletReflectEvent(this));
    }

    private void damage(Bullet bullet) {
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

    private boolean canDamageShip(Ship ship) {
        return this.ship != ship && previousAObject != ship || previousAObject != null && previousAObject != ship;
    }

    @Override
    public boolean canCollideWith(GameObject gameObject) {
        if (SideUtils.IS_SERVER && world.isServer()) {
            return ship != gameObject && previousAObject != gameObject;
        } else {
            return false;
        }
    }
}