package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.engine.physics.PhysicsUtils;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.EntityTypes;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.physics.collision.filter.Filters;
import org.dyn4j.geometry.Geometry;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;

@Getter
public class Wreck extends RigidBody {
    private final ObjectPool<Wreck> wreckPool;

    private WreckData wreckData;
    protected boolean emitFire;
    protected float explosionTimer;
    private int destroyedShipId;

    public Wreck(ObjectPool<Wreck> wreckPool) {
        this.wreckPool = wreckPool;
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    public Wreck init(World world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY, float velocityX,
                      float velocityY, float angularVelocity, int maxLifeTime, float hull, int destroyedShipId, boolean emitFire,
                      WreckData wreckData) {
        this.configData = wreckData;
        this.wreckData = wreckData;
        this.body.setPosition(x, y);
        this.body.setRotation(sin, cos);
        this.body.setLinearVelocity(velocityX, velocityY);
        this.body.setAngularVelocity(angularVelocity);
        setSize(scaleX, scaleY);
        this.maxLifeTime = maxLifeTime;
        this.emitFire = emitFire;
        this.health = hull;
        this.destroyedShipId = destroyedShipId;
        this.lifeTime = 0;
        this.configData = wreckData;
        this.isDead = false;
        init(world, id);
        return this;
    }

    public Wreck init(World world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY, float velocityX,
                      float velocityY, float angularVelocity, int maxLifeTime, int destroyedShipId, boolean emitFire, WreckData wreckData) {
        return init(world, id, x, y, sin, cos, scaleX, scaleY, velocityX, velocityY, angularVelocity, maxLifeTime, 10, destroyedShipId,
                emitFire, wreckData);
    }

    @Override
    protected void initBody() {
        super.initBody();
        removeHullFixtures();
        createFixture();
    }

    private void createFixture() {
        Polygon polygon = Geometry.scale(wreckData.getPolygon(), getSizeX());

        if (getSizeX() != 1.0f) {
            Vector2 centroid = polygon.centroid;
            for (int i = 0; i < polygon.vertices.length; i++) {
                polygon.vertices[i].addLocal(-centroid.x, -centroid.y);
            }

            centroid.setZero();
        }

        addFixture(new Fixture(polygon, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY));
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
    public WreckSpawnData createSpawnData() {
        return new WreckSpawnData();
    }

    @Override
    public void setDead() {
        super.setDead();
        eventBus.publish(new WreckDeathEvent(this));
    }

    @Override
    public void onRemovedFromWorld(int frame) {
        super.onRemovedFromWorld(frame);
        wreckPool.returnBack(this);
    }

    @Override
    public int getEntityType() {
        return EntityTypes.WRECK.ordinal();
    }
}