package net.bfsr.entity.bullet;

import lombok.Getter;
import net.bfsr.common.math.LUT;
import net.bfsr.common.math.MathUtils;
import net.bfsr.config.entity.bullet.BulletData;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.EventBus;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDeathEvent;
import net.bfsr.event.entity.bullet.BulletHitShipEvent;
import net.bfsr.event.entity.bullet.BulletReflectEvent;
import net.bfsr.physics.filter.BulletFilter;
import net.bfsr.util.SideUtils;
import net.bfsr.util.SyncUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

public class Bullet extends RigidBody {
    @Getter
    protected final Ship ship;
    private final float bulletSpeed;
    private final BulletDamage damage;
    private final Polygon polygon;
    @Getter
    private final BulletData bulletData;
    private float energy;
    private Object previousAObject;
    @Getter
    private final float startLifeTime;

    public Bullet(float x, float y, float sin, float cos, Ship ship, BulletData bulletData) {
        super(x, y, sin, cos, bulletData.getSizeX(), bulletData.getSizeY());
        this.ship = ship;
        this.lifeTime = bulletData.getLifeTimeInTicks();
        this.startLifeTime = lifeTime;
        this.bulletSpeed = bulletData.getBulletSpeed();
        this.damage = new BulletDamage(bulletData.getBulletDamage());
        this.energy = damage.getAverage();
        this.polygon = bulletData.getPolygon();
        this.bulletData = bulletData;
        this.velocity.set(cos * bulletSpeed, sin * bulletSpeed);
    }

    @Override
    public void init(World world, int id) {
        super.init(world, id);

        if (SideUtils.IS_SERVER && this.world.isServer()) {
            body.getTransform().setTranslation(position.x + velocity.x / 500.0f, position.y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
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
    public void updateClientPositionFromPacket(Vector2f position, float sin, float cos, Vector2f velocity, float angularVelocity) {
        setRotation(sin, cos);
        SyncUtils.updatePos(this, position);
        body.setLinearVelocity(velocity.x, velocity.y);
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
                        EventBus.post(world.getSide(), new BulletDamageShipHullEvent(this, ship, contactX, contactY));
                        setDead();
                    } else {
                        //Shield reflection
                        damage(this);
                        reflect(normalX, normalY);
                    }

                    EventBus.post(world.getSide(), new BulletHitShipEvent(this, ship, contactX, contactY, normalX, normalY));
                }
            } else if (userData instanceof Bullet bullet) {
                bullet.setDead();
            } else if (userData instanceof Wreck wreck) {
                wreck.damage(damage.getHull());
                setDead();
            } else if (userData instanceof ShipWreck wreck) {
                wreck.bulletDamage(this, contactX, contactY, normalX, normalY);
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
        EventBus.post(world.getSide(), new BulletReflectEvent(this));
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
            energy -= damage;

            if (energy <= 0) {
                setDead();
            }
        }
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

    private boolean damageShip(Ship ship, float contactX, float contactY) {
        return ship.attackShip(damage, ship, contactX, contactY, ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }

    @Override
    public void setDead() {
        super.setDead();
        EventBus.post(world.getSide(), new BulletDeathEvent(this));
    }
}