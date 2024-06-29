package net.bfsr.entity.bullet;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.filter.BulletFilter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;

public class Bullet extends RigidBody {
    @Getter
    protected final RigidBody owner;
    private final float bulletSpeed;
    @Getter
    private final BulletDamage damage;
    private final Polygon polygon;
    @Setter
    @Getter
    private RigidBody lastCollidedRigidBody;
    @Getter
    private final GunData gunData;

    public Bullet(float x, float y, float sin, float cos, GunData gunData, RigidBody owner, BulletDamage damage) {
        super(x, y, sin, cos, gunData.getBulletSizeX(), gunData.getBulletSizeY(), gunData, GunRegistry.INSTANCE.getId());
        this.gunData = gunData;
        this.owner = owner;
        this.maxLifeTime = gunData.getBulletLifeTimeInTicks();
        this.bulletSpeed = gunData.getBulletSpeed();
        this.damage = damage;
        this.health = damage.getAverage();
        this.polygon = gunData.getBulletPolygon();
        this.velocity.set(cos * bulletSpeed, sin * bulletSpeed);
        this.lastCollidedRigidBody = owner;
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

        eventBus.publish(postPhysicsUpdateEvent);
    }

    public void reflect(float normalX, float normalY) {
        float dot = velocity.x * normalX + velocity.y * normalY;
        setVelocity(velocity.x - 2 * dot * normalX, velocity.y - 2 * dot * normalY);
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        setRotation(LUT.sin(rotateToVector), LUT.cos(rotateToVector));
    }

    public void damage() {
        float damage = this.damage.getAverage();
        damage /= 3.0f;

        this.damage.reduceArmor(damage);
        this.damage.reduceHull(damage);
        this.damage.reduceShield(damage);

        if (this.damage.getArmor() < 0) setDead();
        else if (this.damage.getHull() < 0) setDead();
        else if (this.damage.getShield() < 0) setDead();
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new BulletSpawnData(this);
    }

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.BULLET.ordinal();
    }
}