package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;

import java.util.Random;

public class Wreck extends RigidBody<WreckData> {
    public static final ObjectPool<Wreck> WREAK_POOL = new ObjectPool<>(Wreck::new);

    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    protected boolean emitFire;

    @Getter
    protected float explosionTimer;

    protected Random random;

    @Getter
    private int destroyedShipId;

    @Getter
    private WreckType wreckType;
    private float angularVelocity;
    @Getter
    private final EventBus wreckEventBus = new EventBus();

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float sin, float cos,
                      float angularVelocity, float scaleX, float scaleY, int maxLifeTime, int wreckIndex, boolean fire,
                      boolean light, boolean emitFire, float hull, int destroyedShipId, WreckType wreckType,
                      WreckData wreckData) {
        this.position.set(x, y);
        this.velocity.set(velocityX, velocityY);
        this.sin = sin;
        this.cos = cos;
        this.size.set(scaleX, scaleY);
        this.maxLifeTime = maxLifeTime;
        this.wreckIndex = wreckIndex;
        this.emitFire = emitFire;
        this.fire = fire;
        this.light = light;
        this.health = hull;
        this.destroyedShipId = destroyedShipId;
        this.random = world.getRand();
        this.lifeTime = 0;
        this.wreckType = wreckType;
        this.configData = wreckData;
        this.isDead = false;
        this.angularVelocity = angularVelocity;
        init(world, id);
        return this;
    }

    public Wreck init(World world, int id, int wreckIndex, boolean light, boolean fire, boolean emitFire, float x, float y,
                      float velocityX, float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                      int maxLifeTime, WreckType wreckType) {
        WreckData wreck = WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex);
        return init(world, id, x, y, velocityX, velocityY, sin, cos, angularVelocity, scaleX, scaleY, maxLifeTime,
                wreckIndex, fire, light, emitFire, 10, 0, wreckType, wreck);
    }

    @Override
    protected void initBody() {
        while (body.getFixtures().size() > 0) body.removeFixture(0);
        createFixture();
        body.setOwner(null);
        Transform transform = body.getTransform();
        transform.setTranslation(position.x, position.y);
        transform.setRotation(sin, cos);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.setAngularVelocity(angularVelocity);
        setLinearAndAngularDamping();
    }

    private void setLinearAndAngularDamping() {
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    private void createFixture() {
        Polygon p = Geometry.scale(configData.getPolygon(), size.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new ShipFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    protected void updateLifeTime() {
        if (lifeTime / (float) maxLifeTime > 0.8f) {
            lifeTime += 2;
        } else {
            lifeTime++;
        }
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new WreckSpawnData(this);
    }

    @Override
    public void setDead() {
        super.setDead();
        eventBus.publish(new WreckDeathEvent(this));
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        WREAK_POOL.returnBack(this);
    }

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.WRECK.ordinal();
    }
}