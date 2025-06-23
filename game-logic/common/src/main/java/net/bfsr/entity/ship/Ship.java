package net.bfsr.entity.ship;

import gnu.trove.set.hash.THashSet;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import net.bfsr.event.entity.ship.ShipJumpInEvent;
import net.bfsr.event.entity.ship.ShipNewMoveDirectionEvent;
import net.bfsr.event.entity.ship.ShipPostPhysicsUpdate;
import net.bfsr.event.entity.ship.ShipRemoveMoveDirectionEvent;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.common.entity.spawn.ship.ShipSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.collision.filter.Filters;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.function.Consumer;

public class Ship extends DamageableRigidBody {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String owner;

    @Getter
    private final Modules modules = new Modules();

    @Getter
    @Setter
    private Faction faction;

    @Getter
    protected boolean spawned;
    @Getter
    private final int jumpTimeInTicks;
    @Getter
    private int jumpTimer;
    @Getter
    private final Vector2f jumpPosition = new Vector2f();
    @Getter
    @Setter
    private int collisionTimer;
    private int sparksTimer;
    private final int timeToDestroy, maxSparksTimer;

    @Getter
    @Setter
    private boolean controlledByPlayer;

    @Getter
    @Setter
    private RigidBody lastAttacker;

    @Getter
    private final THashSet<Direction> moveDirections = new THashSet<>();
    @Getter
    private Ai ai = Ai.NO_AI;
    @Getter
    @Setter
    private RigidBody target;
    private final Vector2f rotationHelper = new Vector2f();
    @Getter
    private final EventBus shipEventBus = new EventBus();
    @Setter
    private Runnable updateRunnable = this::updateAlive;
    @Getter
    private final ShipData configData;

    public Ship(ShipData shipData) {
        super(shipData.getSizeX(), shipData.getSizeY(), shipData, shipData.getPolygonJTS());
        this.configData = shipData;
        this.timeToDestroy = shipData.getDestroyTimeInTicks();
        this.maxSparksTimer = timeToDestroy / 3;
        this.jumpTimeInTicks = Math.round(Engine.convertSecondsToTicks(0.6f) * ((Math.max(Math.max(getSizeX(), getSizeY()) / 150.0f,
                1.0f))));
        this.jumpTimer = jumpTimeInTicks;
        setJumpPosition();
    }

    @Override
    public void init(World world, int id) {
        super.init(world, id);
        modules.init(this);
    }

    @Override
    protected void initBody() {
        super.initBody();

        List<Shape> convexes = configData.getShapeList();
        for (int i = 0; i < convexes.size(); i++) {
            addHullFixture(setupFixture(new Fixture(convexes.get(i))));
        }

        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    private void addForce(Vector2f rotationVector, float speed) {
        body.applyForceToCenter(rotationVector.x * speed, rotationVector.y * speed);
    }

    public void move(Direction dir) {
        if (dir == Direction.STOP) {
            body.getLinearVelocity().mulLocal(modules.getEngines().getManeuverability() * 0.98f);
            return;
        }

        float sin = getSin();
        float cos = getCos();

        if (dir == Direction.FORWARD) {
            addForce(rotationHelper.set(cos, sin), modules.getEngines().getForwardAcceleration());
        } else if (dir == Direction.BACKWARD) {
            addForce(rotationHelper.set(-cos, -sin), modules.getEngines().getBackwardAcceleration());
        } else if (dir == Direction.LEFT) {
            addForce(rotationHelper.set(sin, -cos), modules.getEngines().getSideAcceleration());
        } else if (dir == Direction.RIGHT) {
            addForce(rotationHelper.set(-sin, cos), modules.getEngines().getSideAcceleration());
        }
    }

    public void addMoveDirection(Direction direction) {
        if (moveDirections.add(direction)) {
            eventBus.publish(new ShipNewMoveDirectionEvent(this, direction));
        }
    }

    public void removeMoveDirection(Direction direction) {
        if (moveDirections.remove(direction)) {
            eventBus.publish(new ShipRemoveMoveDirectionEvent(this, direction));
        }
    }

    public void removeAllMoveDirections() {
        moveDirections.forEach(direction -> {
            eventBus.publish(new ShipRemoveMoveDirectionEvent(this, direction));
            return true;
        });

        moveDirections.clear();
    }

    @Override
    public void initConnectedObject(ConnectedObject<?> connectedObject) {
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            weaponSlot.init(weaponSlot.getId(), this);
        } else {
            super.initConnectedObject(connectedObject);
        }
    }

    @Override
    public void addConnectedObject(ConnectedObject<?> connectedObject) {
        super.addConnectedObject(connectedObject);
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            modules.addWeaponToSlot(weaponSlot.getId(), weaponSlot);
        }
    }

    @Override
    public void removeConnectedObject(ConnectedObject<?> connectedObject) {
        super.removeConnectedObject(connectedObject);
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            modules.removeWeaponSlot(weaponSlot.getId());
        }
    }

    @Override
    public void removeConnectedObject(int index) {
        ConnectedObject<?> connectedObject = getConnectedObjects().get(index);
        super.removeConnectedObject(index);
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            modules.removeWeaponSlot(weaponSlot.getId());
        }
    }

    @Override
    public void update() {
        if (spawned) {
            updateConnectedObjects();
            updateRunnable.run();
            updateLifeTime();
        } else {
            updateJump();
        }
    }

    private void updateAlive() {
        if (collisionTimer > 0) collisionTimer -= 1;

        ai.update();
    }

    private void updateDestroying() {
        sparksTimer -= 1;
        if (sparksTimer <= 0) {
            eventBus.publish(new ShipDestroyingExplosionEvent(this));
            sparksTimer = maxSparksTimer;
        }

        lifeTime++;
    }

    private void updateJump() {
        if (jumpTimer-- == 0) {
            setSpawned();
            RotationHelper.angleToVelocity(getSin(), getCos(), 15.0f, rotationHelper);
            setVelocity(rotationHelper.x, rotationHelper.y);
        }
    }

    @Override
    protected void updateLifeTime() {}

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();

        float maxForwardSpeed = modules.getEngines().getMaxForwardVelocity();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        float magnitudeSquared = body.getLinearVelocity().lengthSquared();

        float maxSideSpeed = maxForwardSpeedSquared * 0.8f;
        if (moveDirections.size() > 0 && !moveDirections.contains(Direction.FORWARD) && magnitudeSquared > maxSideSpeed) {
            float percent = maxSideSpeed / magnitudeSquared;
            getLinearVelocity().mulLocal(percent);
        } else if (magnitudeSquared > maxForwardSpeedSquared) {
            float percent = maxForwardSpeedSquared / magnitudeSquared;
            getLinearVelocity().mulLocal(percent);
        }

        modules.update();

        eventBus.publish(new ShipPostPhysicsUpdate(this));
    }

    public void shoot(Consumer<WeaponSlot> onShotEvent) {
        modules.shoot(onShotEvent);
    }

    @Override
    public void addConnectedObjectFixturesToBody() {
        super.addConnectedObjectFixturesToBody();
        modules.addFixturesToBody();
    }

    @Override
    public void onContourReconstructed(Polygon polygon) {
        if (!DamageSystem.isPolygonConnectedToContour(configData.getReactorPolygon().getVertices(), polygon)) {
            modules.getReactor().setDead();
            return;
        }

        List<net.bfsr.entity.ship.module.engine.Engine> engines = modules.getEngines().getEngines();
        for (int i = 0; i < engines.size(); i++) {
            net.bfsr.entity.ship.module.engine.Engine engine = engines.get(i);
            Vector2[] vertices = ((org.jbox2d.collision.shapes.Polygon) engine.getFixture().getShape()).getVertices();
            if (!DamageSystem.isPolygonConnectedToContour(vertices, polygon)) {
                engine.setDead();
            }
        }

        Shield shield = modules.getShield();
        if (!DamageSystem.isPolygonConnectedToContour(configData.getShieldPolygon().getVertices(), polygon)) {
            shield.setDead();
        }
    }

    @Override
    public ShipSpawnData createSpawnData() {
        return new ShipSpawnData();
    }

    public void setHull(Hull hull) {
        modules.setHull(hull);
    }

    public void setCrew(Crew crew) {
        modules.setCrew(crew);
    }

    public void setShield(Shield shield) {
        modules.setShield(shield);
    }

    public void setEngine(Engines engines) {
        modules.setEngines(engines);
    }

    public void setReactor(Reactor reactor) {
        modules.setReactor(reactor);
    }

    public void setArmor(Armor armor) {
        modules.setArmor(armor);
    }

    public void setCargo(Cargo cargo) {
        modules.setCargo(cargo);
    }

    public Vector2f getWeaponSlotPosition(int id) {
        return configData.getWeaponSlotPositions()[id];
    }

    public void addWeaponToSlot(int id, WeaponSlot slot) {
        modules.addWeaponToSlot(id, slot);
        super.addConnectedObject(slot);
    }

    public WeaponSlot getWeaponSlot(int id) {
        List<WeaponSlot> weaponSlots = modules.getWeaponSlots();
        for (int i1 = 0; i1 < weaponSlots.size(); i1++) {
            WeaponSlot weaponSlot = weaponSlots.get(i1);
            if (weaponSlot.getId() == id) {
                return weaponSlot;
            }
        }

        return null;
    }

    public void setAi(Ai ai) {
        ai.init(this);
        this.ai = ai;
    }

    public void setSpawned() {
        spawned = true;
        world.getPhysicWorld().addBody(body);
        ShipJumpInEvent event = new ShipJumpInEvent(this);
        eventBus.publish(event);
        shipEventBus.publish(event);
    }

    @Override
    public void setRotation(float sin, float cos) {
        super.setRotation(sin, cos);

        if (!spawned) {
            setJumpPosition();
        }
    }

    private void setJumpPosition() {
        float jumpLength = Math.max(getSizeX(), getSizeY()) * 1.25f + 2.5f;
        RotationHelper.angleToVelocity(getSin(), getCos(), -jumpLength, jumpPosition);
        jumpPosition.add(getX(), getY());
    }

    public void setDestroying() {
        if (maxLifeTime == DEFAULT_MAX_LIFE_TIME_IN_TICKS) {
            maxLifeTime = timeToDestroy;
            eventBus.publish(new ShipDestroyingEvent(this));
            updateRunnable = this::updateDestroying;
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        eventBus.publish(new ShipDestroyEvent(this));
    }

    public boolean isDestroying() {
        return maxLifeTime != DEFAULT_MAX_LIFE_TIME_IN_TICKS;
    }

    public boolean isBot() {
        return owner == null;
    }

    @Override
    public int getCollisionMatrixId() {
        return CollisionMatrixType.SHIP.ordinal();
    }

    @Override
    public Filter getCollisionFilter(Fixture fixture) {
        return Filters.SHIP_FILTER;
    }
}