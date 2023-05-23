package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.EventBus;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import net.bfsr.event.entity.wreck.WreckUpdateEvent;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.WreckFilter;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;

import java.util.Random;

public class Wreck extends RigidBody {
    @Getter
    protected float lifeTimeVelocity;
    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    protected boolean emitFire;

    @Getter
    protected float explosionTimer, hull;

    protected Random random;

    @Getter
    private int destroyedShipId;

    @Getter
    private WreckType wreckType;
    @Getter
    protected WreckData wreckData;

    public Wreck init(World world, int id, float x, float y, float velocityX, float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                      float lifeTimeVelocity, int wreckIndex, boolean fire, boolean light, boolean emitFire, float hull, int destroyedShipId, WreckType wreckType,
                      WreckData wreckData) {
        this.world = world;
        this.id = id;
        this.position.set(x, y);
        this.velocity.set(velocityX, velocityY);
        this.sin = sin;
        this.cos = cos;
        this.size.set(scaleX, scaleY);
        this.lifeTimeVelocity = lifeTimeVelocity;
        this.wreckIndex = wreckIndex;
        this.emitFire = emitFire;
        this.fire = fire;
        this.light = light;
        this.hull = hull;
        this.destroyedShipId = destroyedShipId;
        this.random = world.getRand();
        this.lifeTime = 0;
        this.wreckType = wreckType;
        this.wreckData = wreckData;
        this.isDead = false;
        createFixtures(angularVelocity);
        return this;
    }

    public Wreck init(World world, int id, int wreckIndex, boolean light, boolean fire, boolean emitFire, float x, float y, float velocityX, float velocityY,
                      float sin, float cos, float angularVelocity, float scaleX, float scaleY, float lifeTimeVelocity, WreckType wreckType) {
        WreckData wreck = WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex);
        return init(world, id, x, y, velocityX, velocityY, sin, cos, angularVelocity, scaleX, scaleY, lifeTimeVelocity, wreckIndex, fire, light, emitFire, 10,
                0, wreckType, wreck);
    }

    @Override
    protected void initBody() {
        createFixtures(0.0f);
    }

    private void createFixtures(float angularVelocity) {
        while (body.getFixtures().size() > 0) body.removeFixture(0);
        createFixture();
        body.setOwner(null);
        body.translate(position.x, position.y);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(sin, cos);
        body.setAngularVelocity(angularVelocity);
        setLinearAndAngularDamping();
    }

    private void setLinearAndAngularDamping() {
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    private void createFixture() {
        Polygon p = Geometry.scale(wreckData.getPolygon(), size.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    public void update() {
        updateLifeTime();
    }

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        EventBus.post(world.getSide(), new WreckUpdateEvent(this));
    }

    private void updateLifeTime() {
        if (lifeTime > 0.8f) {
            lifeTime += lifeTimeVelocity * 0.05f;
            if (lifeTime >= 1.0f) {
                onLifeTimeEnded();
            }
        } else {
            lifeTime += lifeTimeVelocity * TimeUtils.UPDATE_DELTA_TIME;
        }
    }

    private void onLifeTimeEnded() {
        setDead();
    }

    public void damage(float damage) {
        hull -= damage;
        onHullDamage();
    }

    private void onHullDamage() {
        if (hull <= 0) {
            setDead();
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        EventBus.post(world.getSide(), new WreckDeathEvent(this));
    }
}