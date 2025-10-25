package net.bfsr.entity.bullet;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.collision.filter.Filters;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector4f;

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
    @Getter
    private final Vector4f spawnTransform;

    private Runnable postPhysicsRotationUpdater = RunnableUtils.EMPTY_RUNNABLE;
    @Setter
    @Getter
    private int clientId;

    public Bullet(float x, float y, float sin, float cos, GunData gunData, RigidBody owner, BulletDamage damage) {
        super(x, y, sin, cos, gunData.getBulletSizeX(), gunData.getBulletSizeY(), gunData);
        this.gunData = gunData;
        this.owner = owner;
        this.maxLifeTime = gunData.getBulletLifeTimeInFrames();
        this.bulletSpeed = gunData.getBulletSpeed();
        this.damage = damage;
        this.health = damage.getAverage();
        this.polygon = gunData.getBulletPolygon();
        this.body.setLinearVelocity(cos * bulletSpeed, sin * bulletSpeed);
        this.body.setBullet(true);
        this.lastCollidedRigidBody = owner;
        this.spawnTransform = new Vector4f(x, y, sin, cos);
    }

    @Override
    protected void initBody() {
        super.initBody();
        addFixture(new Fixture(polygon, Filters.BULLET_FILTER, this, 0.0f));
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
        super.postPhysicsUpdate();
        postPhysicsRotationUpdater.run();
    }

    public void reflect(float normalX, float normalY) {
        Vector2 velocity = getLinearVelocity();
        float dot = velocity.x * normalX + velocity.y * normalY;
        setVelocity(velocity.x - 2 * dot * normalX, velocity.y - 2 * dot * normalY);
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        postPhysicsRotationUpdater = () -> {
            setRotation(LUT.sin(rotateToVector), LUT.cos(rotateToVector));
            postPhysicsRotationUpdater = RunnableUtils.EMPTY_RUNNABLE;
        };
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
    public BulletSpawnData createSpawnData() {
        return new BulletSpawnData();
    }

    @Override
    public int getCollisionMatrixId() {
        return CollisionMatrixType.BULLET.ordinal();
    }
}