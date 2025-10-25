package net.bfsr.engine.world.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.entity.GameObjectConfigData;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.engine.event.entity.RigidBodyPostPhysicsUpdateEvent;
import net.bfsr.engine.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.engine.physics.PhysicsUtils;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.world.World;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class RigidBody extends GameObject {
    protected static final int DEFAULT_MAX_LIFE_TIME_IN_FRAMES = Engine.convertIntSecondsToFrames(20);

    @Getter
    protected World world;
    @Getter
    protected Body body = new Body();
    @Getter
    @Setter
    protected int id;
    @Getter
    protected int lifeTime, maxLifeTime = DEFAULT_MAX_LIFE_TIME_IN_FRAMES;
    protected EventBus eventBus;
    @Setter
    @Getter
    protected GameObjectConfigData configData;
    @Setter
    @Getter
    protected float health;
    private final RigidBodyPostPhysicsUpdateEvent postPhysicsUpdateEvent = new RigidBodyPostPhysicsUpdateEvent(this);
    @Getter
    private CorrectionHandler correctionHandler = new HistoryCorrectionHandler();
    private final List<Fixture> fixturesToAdd = new ArrayList<>();
    private final List<Fixture> fixturesToRemove = new ArrayList<>();
    private final List<Fixture> hullFixturesToAdd = new ArrayList<>();
    private final List<Fixture> hullFixtures = new ArrayList<>();

    public RigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, GameObjectConfigData configData) {
        super(x, y, sizeX, sizeY);
        this.body.setPosition(x, y);
        this.body.setRotation(sin, cos);
        this.body.setUserData(this);
        this.configData = configData;
    }

    public RigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY) {
        this(x, y, sin, cos, sizeX, sizeY, null);
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
        this.correctionHandler.setRigidBody(this);
        initBody();
    }

    protected void initBody() {
        body.world = world.getPhysicWorld();
        body.setActive(true);
    }

    public Fixture setupFixture(Fixture bodyFixture) {
        bodyFixture.setUserData(this);
        bodyFixture.setFilter(getCollisionFilter(bodyFixture));
        bodyFixture.setDensity(getFixtureDensity(bodyFixture));
        return bodyFixture;
    }

    @Override
    public void update() {
        updateLifeTime();
    }

    protected void updateLifeTime() {
        lifeTime++;
    }

    void processFixturesToRemove() {
        if (fixturesToRemove.size() > 0) {
            body.removeFixtures(fixturesToRemove);
            fixturesToRemove.clear();
        }
    }

    void processFixturesToAdd() {
        if (hullFixturesToAdd.size() > 0) {
            for (int i = 0; i < hullFixturesToAdd.size(); i++) {
                Fixture fixture = hullFixturesToAdd.get(i);
                fixturesToAdd.add(fixture);
                hullFixtures.add(fixture);
            }

            hullFixturesToAdd.clear();
        }

        if (fixturesToAdd.size() > 0) {
            body.addFixtures(fixturesToAdd);
            fixturesToAdd.clear();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        eventBus.publishOptimized(postPhysicsUpdateEvent);
    }

    public void onAddedToWorld() {
        eventBus.publish(new RigidBodyAddToWorldEvent(this));
    }

    public void onRemovedFromWorld(int frame) {
        eventBus.publish(new RigidBodyRemovedFromWorldEvent(this, frame));
    }

    public void addFixture(Fixture fixture) {
        if (world.getPhysicWorld().isLocked()) {
            fixturesToAdd.add(fixture);
        } else {
            body.addFixture(fixture);
        }
    }

    public void addHullFixture(Fixture fixture) {
        if (world.getPhysicWorld().isLocked()) {
            hullFixturesToAdd.add(fixture);
        } else {
            hullFixtures.add(fixture);
            body.addFixture(fixture);
        }
    }

    public void removeFixture(Fixture fixture) {
        if (world.getPhysicWorld().isLocked()) {
            fixturesToRemove.add(fixture);
        } else {
            body.removeFixture(fixture);
        }
    }

    public void removeHullFixtures() {
        hullFixturesToAdd.clear();

        if (world.getPhysicWorld().isLocked()) {
            for (int i = 0; i < hullFixtures.size(); i++) {
                fixturesToRemove.add(hullFixtures.get(i));
            }
        } else {
            body.removeFixtures(hullFixtures);
        }

        hullFixtures.clear();
    }

    public RigidBodySpawnData<?> createSpawnData() {
        return new RigidBodySpawnData<>();
    }

    @Override
    public void setPosition(float x, float y) {
        body.setPosition(x, y);
    }

    public void setRotation(float sin, float cos) {
        body.setRotation(sin, cos);
    }

    public void setTransform(float x, float y, float sin, float cos) {
        body.setTransform(x, y, sin, cos);
    }

    public void setVelocity(float x, float y) {
        body.setLinearVelocity(x, y);
    }

    public void setAngularVelocity(float angularVelocity) {
        body.setAngularVelocity(angularVelocity);
    }

    public void setLinearVelocity(Vector2 velocity) {
        body.setLinearVelocity(velocity);
    }

    public void setCorrectionHandler(CorrectionHandler correctionHandler) {
        this.correctionHandler = correctionHandler;
        correctionHandler.setRigidBody(this);
    }

    @Override
    public float getX() {
        return body.getTransform().getX();
    }

    @Override
    public float getY() {
        return body.getTransform().getY();
    }

    public float getSin() {
        return body.getTransform().getSin();
    }

    public float getCos() {
        return body.getTransform().getCos();
    }

    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    public int getRegistryId() {
        return configData.getRegistryId();
    }

    public int getDataId() {
        return configData.getId();
    }

    public int getCollisionMatrixId() {
        return 0;
    }

    public Filter getCollisionFilter(Fixture fixture) {
        return PhysicsUtils.DEFAULT_COLLISION_FILTER;
    }

    public float getFixtureDensity(Fixture fixture) {
        return PhysicsUtils.DEFAULT_FIXTURE_DENSITY;
    }
}