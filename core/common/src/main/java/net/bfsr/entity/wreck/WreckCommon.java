package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.entity.CollisionObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;

import java.util.Random;

public abstract class WreckCommon extends CollisionObject implements TOITransformSavable {
    @Getter
    protected float alphaVelocity;
    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    protected boolean fireExplosion;
    protected boolean fireFadingOut;

    @Getter
    protected float explosionTimer, sparkleBlinkTimer, hull;

    protected Random random;

    @Getter
    private int destroyedShipId;
    /**
     * Saved transform before TOI solver
     */
    private final Transform transform = new Transform();
    private boolean transformSaved;
    @Getter
    private WreckType wreckType;
    protected RegisteredShipWreck registeredShipWreck;

    public WreckCommon init(World<?> world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float r, float g, float b,
                            float a, float alphaVelocity, int wreckIndex, boolean fire, boolean light, boolean fireExplosion, float hull, int destroyedShipId,
                            WreckType wreckType, RegisteredShipWreck registeredShipWreck) {
        this.world = world;
        this.id = id;
        this.position.set(x, y);
        this.lastPosition.set(position);
        this.velocity.set(velocityX, velocityY);
        this.rotation = rotation;
        this.lastRotation = rotation;
        this.scale.set(scaleX, scaleY);
        this.lastScale.set(scale);
        this.color.set(r, g, b, a);
        this.alphaVelocity = alphaVelocity;
        this.wreckIndex = wreckIndex;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        this.light = light;
        this.hull = hull;
        this.destroyedShipId = destroyedShipId;
        this.random = world.getRand();
        this.aliveTimer = 0;
        this.wreckType = wreckType;
        this.registeredShipWreck = registeredShipWreck;
        this.isDead = false;
        createBody(x, y, angularVelocity);
        createAABB();
        addParticle();
        return this;
    }

    protected abstract void addParticle();

    @Override
    protected void createBody(float x, float y) {
        createBody(x, y, 0.0f);
    }

    protected void createBody(float x, float y, float angularVelocity) {
        createFixtures();
        body.translate(x, y);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rotation);
        body.setAngularVelocity(angularVelocity);
        setLinearAndAngularDamping();

        world.addPhysicObject(this);
    }

    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    protected void createFixtures() {
        if (body.getFixtures().size() > 0) body.removeFixture(0);
        Polygon p = Geometry.scale(registeredShipWreck.getPolygon(), scale.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    public void update() {
        updateLifeTime();
    }

    protected void updateLifeTime() {
        if (color.w < 0.2f) {
            color.w -= alphaVelocity * 0.05f;
            if (color.w <= 0.0f) {
                color.w = 0.0f;
                onLifeTimeEnded();
            }
        } else {
            color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
            if (color.w < 0.0f) color.w = 0.0f;
        }
    }

    protected void onLifeTimeEnded() {

    }

    public void postPhysicsUpdate() {
        if (transformSaved) {
            body.setTransform(transform);
            transformSaved = false;
        }

        super.postPhysicsUpdate();
    }

    public void damage(float damage) {
        hull -= damage;
        onHullDamage();
    }

    protected void onHullDamage() {}

    @Override
    public void saveTransform(Transform transform) {
        this.transform.set(transform);
        transformSaved = true;
    }

    protected void destroy() {
        setDead(true);
    }

    public void onRemoved() {

    }

    public void renderAdditive() {

    }
}
