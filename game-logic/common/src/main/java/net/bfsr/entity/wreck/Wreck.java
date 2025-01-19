package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.Filters;
import net.bfsr.world.World;
import org.dyn4j.geometry.Geometry;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.dynamics.Fixture;

@Getter
public class Wreck extends RigidBody {
    private int wreckIndex;

    protected boolean fire;
    protected boolean light;
    protected boolean emitFire;

    protected float explosionTimer;

    private int destroyedShipId;

    private WreckType wreckType;
    private final EventBus wreckEventBus = new EventBus();
    private WreckData wreckData;

    public Wreck() {
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float sin, float cos,
                      float angularVelocity, float scaleX, float scaleY, int maxLifeTime, int wreckIndex, boolean fire,
                      boolean light, boolean emitFire, float hull, int destroyedShipId, WreckType wreckType,
                      WreckData wreckData) {
        this.wreckData = wreckData;
        this.body.setPosition(x, y);
        this.body.setRotation(sin, cos);
        this.body.setLinearVelocity(velocityX, velocityY);
        this.body.setAngularVelocity(angularVelocity);
        setSize(scaleX, scaleY);
        this.maxLifeTime = maxLifeTime;
        this.wreckIndex = wreckIndex;
        this.emitFire = emitFire;
        this.fire = fire;
        this.light = light;
        this.health = hull;
        this.destroyedShipId = destroyedShipId;
        this.lifeTime = 0;
        this.wreckType = wreckType;
        this.configData = wreckData;
        this.isDead = false;
        init(world, id);
        return this;
    }

    public Wreck init(World world, int id, int wreckIndex, boolean light, boolean fire, boolean emitFire, float x, float y,
                      float velocityX, float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                      int maxLifeTime, WreckType wreckType, WreckData wreckData) {
        return init(world, id, x, y, velocityX, velocityY, sin, cos, angularVelocity, scaleX, scaleY, maxLifeTime,
                wreckIndex, fire, light, emitFire, 10, 0, wreckType, wreckData);
    }

    @Override
    protected void initBody() {
        super.initBody();
        removeHullFixtures();
        createFixture();
    }

    private void createFixture() {
        Polygon p = Geometry.scale(wreckData.getPolygon(), getSizeX());
        addFixture(new Fixture(p, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY));
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
        world.getObjectPools().getWrecksPool().returnBack(this);
    }

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.WRECK.ordinal();
    }
}