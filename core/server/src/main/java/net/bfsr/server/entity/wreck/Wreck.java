package net.bfsr.server.entity.wreck;

import lombok.Getter;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.server.collision.filter.WreckFilter;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.server.PacketRemoveObject;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;

import java.util.Random;

public class Wreck extends CollisionObject implements TOITransformSavable {
    protected float maxLifeTime;
    @Getter
    protected float lifeTimeVelocity;
    @Getter
    private int wreckIndex;

    @Getter
    protected boolean fire;
    @Getter
    protected boolean light;
    @Getter
    protected boolean fireExplosion;

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

    public Wreck init(WorldServer world, int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                      float lifeTime, float lifeTimeVelocity, int wreckIndex, boolean fire, boolean light, boolean fireExplosion, float hull, int destroyedShipId,
                      WreckType wreckType, RegisteredShipWreck registeredShipWreck) {
        this.world = world;
        this.id = id;
        this.position.set(x, y);
        this.velocity.set(velocityX, velocityY);
        this.rotation = rotation;
        this.scale.set(scaleX, scaleY);
        this.lifeTimeVelocity = lifeTimeVelocity;
        this.wreckIndex = wreckIndex;
        this.fireExplosion = fireExplosion;
        this.fire = fire;
        this.light = light;
        this.hull = hull;
        this.destroyedShipId = destroyedShipId;
        this.random = world.getRand();
        this.lifeTime = 0;
        this.maxLifeTime = lifeTime;
        this.wreckType = wreckType;
        this.registeredShipWreck = registeredShipWreck;
        this.isDead = false;
        createFixtures(angularVelocity);
        world.addWreck(this);
        return this;
    }

    public Wreck init(WorldServer world, int id, int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY,
                      float rotation, float angularVelocity, float scaleX, float scaleY, float lifeTime, float lifeTimeVelocity, WreckType wreckType) {
        return init(world, id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, lifeTime, lifeTimeVelocity, wreckIndex, fire, light,
                fireExplosion, 10, 0, wreckType, WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex));
    }

    @Override
    protected void initBody() {
        createFixtures(0.0f);
    }

    protected void createFixtures(float angularVelocity) {
        while (body.getFixtures().size() > 0) body.removeFixture(0);
        createFixture();
        body.translate(position.x, position.y);
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

    protected void createFixture() {
        Polygon p = Geometry.scale(registeredShipWreck.getPolygon(), scale.x);
        BodyFixture bodyFixture = new BodyFixture(p);
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    public void update() {
        updateLifeTime();
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketObjectPosition(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    protected void updateLifeTime() {
        if (lifeTime > 0.8f) {
            lifeTime += lifeTimeVelocity * 0.05f;
            if (lifeTime >= 1.0f) {
                onLifeTimeEnded();
            }
        } else {
            lifeTime += lifeTimeVelocity * TimeUtils.UPDATE_DELTA_TIME;
        }
    }

    protected void onLifeTimeEnded() {
        destroy();
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

    protected void onHullDamage() {
        if (hull <= 0) {
            destroy();
        }
    }

    @Override
    public void saveTransform(Transform transform) {
        this.transform.set(transform);
        transformSaved = true;
    }

    protected void destroy() {
        setDead(true);
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }
}