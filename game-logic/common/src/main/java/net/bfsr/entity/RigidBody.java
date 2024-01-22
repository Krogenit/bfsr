package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.event.EventBusManager;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyDeathEvent;
import net.bfsr.event.entity.RigidBodyPostPhysicsUpdateEvent;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.RigidBodySpawnData;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

@NoArgsConstructor
public class RigidBody<CONFIG_DATA extends GameObjectConfigData> extends GameObject {
    @Getter
    protected World world;
    @Getter
    protected final Body body = new Body();
    @Getter
    @Setter
    protected int id;
    @Getter
    protected Vector2f velocity = new Vector2f();
    @Getter
    protected float lifeTime, maxLifeTime = 1200;
    @Getter
    protected float sin, cos;
    private final Transform savedTransform = new Transform();
    protected EventBusManager eventBus;
    @Setter
    @Getter
    protected CONFIG_DATA configData;
    @Setter
    @Getter
    protected int registryId;
    @Setter
    protected float health;

    protected final RigidBodyPostPhysicsUpdateEvent postPhysicsUpdateEvent = new RigidBodyPostPhysicsUpdateEvent(this);

    public RigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, CONFIG_DATA configData, int registryId) {
        super(x, y, sizeX, sizeY);
        this.sin = sin;
        this.cos = cos;
        this.body.getTransform().setTranslation(x, y);
        this.body.getTransform().setRotation(sin, cos);
        this.configData = configData;
        this.registryId = registryId;
    }

    public RigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY) {
        this(x, y, sin, cos, sizeX, sizeY, null, -1);
    }

    protected RigidBody(float x, float y, float sizeX, float sizeY) {
        this(x, y, 0.0f, 1.0f, sizeX, sizeY);
    }

    protected RigidBody(float sizeX, float sizeY) {
        this(0, 0, sizeX, sizeY);
    }

    public void init(World world, int id) {
        this.world = world;
        this.id = id;
        this.eventBus = world.getEventBus();
        this.eventBus.optimizeEvent(postPhysicsUpdateEvent);
        initBody();
    }

    protected void initBody() {}

    public BodyFixture setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setUserData(this);
        bodyFixture.setFilter(new ShipFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        return bodyFixture;
    }

    @Override
    public void update() {
        updateLifeTime();
    }

    protected void updateLifeTime() {
        lifeTime++;

        if (SideUtils.IS_SERVER && world.isServer()) {
            if (lifeTime >= maxLifeTime) {
                setDead();
            }
        } else {
            if (lifeTime >= 60) {
                setDead();
            }
        }
    }

    @Override
    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
        Vector2 vel = body.getLinearVelocity();
        velocity.x = (float) vel.x;
        velocity.y = (float) vel.y;

        eventBus.publishOptimized(postPhysicsUpdateEvent);
    }

    public void onAddedToWorld() {
        eventBus.publish(new RigidBodyAddToWorldEvent(this));
    }

    public void onRemovedFromWorld() {}

    public void collision(Body body, BodyFixture fixture, float contactX, float contactY, float normalX, float normalY,
                          ContactCollisionData<Body> collision) {}

    public boolean canCollideWith(GameObject gameObject) {
        return this != gameObject;
    }

    public EntityPacketSpawnData createSpawnData() {
        return new RigidBodySpawnData(this);
    }

    public void damage(float amount, float contactX, float contactY, float normalX, float normalY) {
        health -= amount;
        if (health <= 0) {
            setDead();
        }
    }

    public void saveTransform(Transform transform) {
        savedTransform.set(transform);
    }

    public void restoreTransform() {
        body.setTransform(savedTransform);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        body.getTransform().setTranslation(x, y);
    }

    public void setRotation(float sin, float cos) {
        this.sin = sin;
        this.cos = cos;
        body.getTransform().setRotation(sin, cos);
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
        body.setLinearVelocity(x, y);
    }

    private void setAngularVelocity(float angularVelocity) {
        body.setAngularVelocity(angularVelocity);
    }

    @Override
    public void setDead() {
        super.setDead();
        eventBus.publish(new RigidBodyDeathEvent(this));
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getAngularVelocity() {
        return (float) body.getAngularVelocity();
    }

    public int getDataId() {
        return configData.getId();
    }

    public void calcPosition(double timestamp) {
        TransformData transformData = world.getEntityManager().getDataHistoryManager().getTransformData(id, timestamp);
        if (transformData != null) {
            Vector2f epdPosition = transformData.getPosition();
            setPosition(epdPosition.x, epdPosition.y);
            setRotation(transformData.getSin(), transformData.getCos());
        }
    }

    public void processChronologicalData(double timestamp) {
        PacketWorldSnapshot.EntityData entityData = world.getEntityManager().getDataHistoryManager().getData(id, timestamp);
        if (entityData != null) {
            Vector2f velocity = entityData.getVelocity();
            setVelocity(velocity.x, velocity.y);
            setAngularVelocity(entityData.getAngularVelocity());
        }
    }
}