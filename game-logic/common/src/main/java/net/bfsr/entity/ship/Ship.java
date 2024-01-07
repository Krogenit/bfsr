package net.bfsr.entity.ship;

import clipper2.core.PathD;
import gnu.trove.set.hash.THashSet;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiAttackTarget;
import net.bfsr.ai.task.AiSearchTarget;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.ship.*;
import net.bfsr.event.module.shield.ShieldDamageByCollision;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipSpawnData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.Force;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Wound;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Consumer;

import static net.bfsr.math.RigidBodyUtils.ANGLE_TO_VELOCITY;

public class Ship extends DamageableRigidBody<ShipData> {
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
    private final int jumpTimeInTicks = net.bfsr.engine.Engine.convertToTicks(0.6f);
    @Getter
    private int jumpTimer;
    @Getter
    private final Vector2f jumpPosition = new Vector2f();
    private int collisionTimer;
    @Getter
    private int destroyingTimer;
    private int sparksTimer;
    private final int timeToDestroy, maxSparksTimer;

    @Getter
    @Setter
    private boolean controlledByPlayer;

    @Getter
    private RigidBody<?> lastAttacker;

    @Getter
    private final THashSet<Direction> moveDirections = new THashSet<>();
    @Getter
    private Ai ai;
    @Getter
    @Setter
    private RigidBody<?> target;
    @Setter
    private Consumer<Double> positionCalculator = super::calcPosition;
    @Setter
    private Consumer<Double> chronologicalDataProcessor = super::processChronologicalData;

    public Ship(ShipData shipData) {
        super(shipData.getSizeX(), shipData.getSizeY(), shipData, ShipRegistry.INSTANCE.getId(), new DamageMask(32, 32),
                shipData.getContour());
        this.timeToDestroy = shipData.getDestroyTimeInTicks();
        this.maxSparksTimer = timeToDestroy / 3;
        this.jumpTimer = jumpTimeInTicks;
        setJumpPosition();
    }

    @Override
    public void init(World world, int id) {
        super.init(world, id);
        modules.init(this);

        if (SideUtils.IS_SERVER && world.isServer()) {
            addAI();
        }
    }

    @Override
    protected void initBody() {
        List<Convex> convexes = configData.getConvexList();
        for (int i = 0; i < convexes.size(); i++) {
            body.addFixture(setupFixture(new BodyFixture(convexes.get(i))));
        }

        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    private void addForce(Vector2f rotationVector, float speed) {
        body.applyForce(new Force(rotationVector.x * speed, rotationVector.y * speed));
    }

    public void move(Direction dir) {
        if (dir == Direction.STOP) {
            body.getLinearVelocity().multiply(modules.getEngines().getManeuverability() * 0.98f);
            return;
        }

        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();

        if (dir == Direction.FORWARD) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(cos, sin), modules.getEngines().getForwardAcceleration());
        } else if (dir == Direction.BACKWARD) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(-cos, -sin), modules.getEngines().getBackwardAcceleration());
        } else if (dir == Direction.LEFT) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(sin, -cos), modules.getEngines().getSideAcceleration());
        } else if (dir == Direction.RIGHT) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(-sin, cos), modules.getEngines().getSideAcceleration());
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
    public void addConnectedObject(ConnectedObject connectedObject) {
        super.addConnectedObject(connectedObject);
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            modules.addWeaponToSlot(weaponSlot.getId(), weaponSlot);
        }
    }

    @Override
    public void removeConnectedObject(ConnectedObject connectedObject) {
        super.removeConnectedObject(connectedObject);
        if (connectedObject instanceof WeaponSlot weaponSlot) {
            modules.removeWeaponSlot(weaponSlot.getId());
        }
    }

    @Override
    public void collision(Body body, BodyFixture fixture, float contactX, float contactY, float normalX, float normalY,
                          ContactCollisionData<Body> collision) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship otherShip) {
                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400.0f;

                if (impactPowerForOther > 0.25f)
                    otherShip.damageByCollision(this, impactPowerForOther, contactX, contactY, normalX, normalY);
            } else if (userData instanceof Wreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = net.bfsr.engine.Engine.convertToTicks(2);
                    eventBus.publish(new ShipCollisionWithWreckEvent(this, contactX, contactY, normalX, normalY));
                }
            }
        }
    }

    @Override
    public void update() {
        if (spawned) {
            updateFixtures();
            updateShip();
            updateLifeTime();
        } else {
            updateJump();
        }
    }

    private void updateShip() {
        if (collisionTimer > 0) collisionTimer -= 1;

        if (destroyingTimer > 0) {
            sparksTimer -= 1;
            if (sparksTimer <= 0) {
                eventBus.publish(new ShipDestroyingExplosionEvent(this));
                sparksTimer = maxSparksTimer;
            }

            if (SideUtils.IS_SERVER && world.isServer()) {
                destroyingTimer -= 1;
                if (destroyingTimer <= 0) {
                    setDead();
                }
            }
        } else {
            if (ai != null) {
                ai.update();
            }
        }
    }

    @Override
    protected void updateFixtures() {
        if (fixturesToAdd.size() > 0) {
            body.removeAllFixtures();
            for (int i = 0; i < fixturesToAdd.size(); i++) {
                body.addFixture(fixturesToAdd.get(i));
            }
            addConnectedObjectFixturesToBody();
            fixturesToAdd.clear();
            fixturesToRemove.clear();
        }

        if (fixturesToRemove.size() > 0) {
            for (int i = 0; i < fixturesToRemove.size(); i++) {
                body.removeFixture(fixturesToRemove.get(i));
            }
            fixturesToRemove.clear();
        }
    }

    private void updateJump() {
        if (jumpTimer-- == 0) {
            setSpawned();
            RotationHelper.angleToVelocity(sin, cos, 15.0f, ANGLE_TO_VELOCITY);
            setVelocity(ANGLE_TO_VELOCITY.x, ANGLE_TO_VELOCITY.y);
        }
    }

    @Override
    protected void updateLifeTime() {
        if (world.isClient() && lifeTime++ >= 60) {
            setDead();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();

        float maxForwardSpeed = modules.getEngines().getMaxForwardVelocity();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        double magnitudeSquared = body.getLinearVelocity().getMagnitudeSquared();

        float maxSideSpeed = maxForwardSpeedSquared * 0.8f;
        if (moveDirections.size() > 0 && !moveDirections.contains(Direction.FORWARD) && magnitudeSquared > maxSideSpeed) {
            double percent = maxSideSpeed / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        } else if (magnitudeSquared > maxForwardSpeedSquared) {
            double percent = maxForwardSpeedSquared / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        }

        modules.update();

        eventBus.publish(new ShipPostPhysicsUpdate(this));
    }

    public void shoot(Consumer<WeaponSlot> onShotEvent) {
        modules.shoot(onShotEvent);
    }

    private void damageByCollision(Ship otherShip, float impactPower, float contactX, float contactY, float normalX,
                                   float normalY) {
        if (collisionTimer > 0) {
            return;
        }

        lastAttacker = otherShip;
        if (otherShip.faction == faction) {
            impactPower /= 2.0f;
        }

        collisionTimer = net.bfsr.engine.Engine.convertToTicks(2);

        Shield shield = modules.getShield();
        if (shield != null && shield.damage(impactPower)) {
            onShieldDamageByCollision(contactX, contactY, normalX, normalY);
            return;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;

        float reducedHullDamage = modules.getArmor().reduceDamageByArmor(armorDamage, hullDamage, contactX, contactY, this);
        modules.getHull().damage(reducedHullDamage, contactX, contactY, this);
        onHullDamageByCollision(contactX, contactY, normalX, normalY);
    }

    private void onShieldDamageByCollision(float contactX, float contactY, float normalX, float normalY) {
        eventBus.publish(new ShieldDamageByCollision(this, contactX, contactY, normalX, normalY));
    }

    private void onHullDamageByCollision(float contactX, float contactY, float normalX, float normalY) {
        eventBus.publish(new ShipHullDamageByCollisionEvent(this, contactX, contactY, normalX, normalY));
    }

    public void damage(BulletDamage damage, Ship attacker, float contactX, float contactY, float multiplayer,
                       BodyFixture fixture, Runnable onShieldDamageRunnable, Runnable onArmorDamageRunnable,
                       Runnable onHullDamageRunnable) {
        lastAttacker = attacker;
        float shieldDamage = damage.getShield() * multiplayer;

        Shield shield = modules.getShield();
        if (shield != null && shield.damage(shieldDamage)) {
            onShieldDamageRunnable.run();
            return;
        }

        float hullDamage = damage.getHull() * multiplayer;
        float armorDamage = damage.getArmor() * multiplayer;

        float reducedHullDamage = modules.getArmor().reduceDamageByArmor(armorDamage, hullDamage, contactX, contactY, this);

        if (reducedHullDamage == hullDamage) {
            HullCell cell = modules.getHull().damage(reducedHullDamage, contactX, contactY, this);

            if (SideUtils.IS_SERVER && world.isServer()) {
                Object userData = fixture.getUserData();
                if (userData instanceof DamageableModule) {
                    ((DamageableModule) userData).damage(reducedHullDamage);
                }
            }

            onHullDamageRunnable.run();
            eventBus.publish(new ShipHullDamageEvent(this, contactX, contactY, cell));
        } else {
            onArmorDamageRunnable.run();
        }
    }

    @Override
    public void addConnectedObjectFixturesToBody() {
        super.addConnectedObjectFixturesToBody();
        modules.addFixturesToBody();
    }

    @Override
    public void onContourReconstructed(PathD contour) {
        if (!DamageSystem.isPolygonConnectedToContour(configData.getReactorPolygon().getVertices(), contour)) {
            modules.getReactor().setDead();
            return;
        }

        List<Engine> engines = modules.getEngines().getEngines();
        for (int i = 0; i < engines.size(); i++) {
            Engine engine = engines.get(i);
            Vector2[] vertices = ((Wound) engine.getFixture().getShape()).getVertices();
            if (!DamageSystem.isPolygonConnectedToContour(vertices, contour)) {
                engine.setDead();
            }
        }

        Shield shield = modules.getShield();
        if (!DamageSystem.isPolygonConnectedToContour(configData.getShieldPolygon().getVertices(), contour)) {
            shield.setDead();
        }
    }

    @Override
    public void calcPosition(double timestamp) {
        positionCalculator.accept(timestamp);
    }

    @Override
    public void processChronologicalData(double timestamp) {
        chronologicalDataProcessor.accept(timestamp);
    }

    public void resetPositionCalculatorAndChronologicalProcessor() {
        positionCalculator = super::calcPosition;
        chronologicalDataProcessor = super::processChronologicalData;
    }

    public void addAI() {
        this.ai = new Ai();
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
    }

    public void removeAI() {
        this.ai = null;
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new ShipSpawnData(this);
    }

    public void setHull(Hull hull) {
        modules.setHull(hull);
    }

    protected void setCrew(Crew crew) {
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

    public void setSpawned() {
        spawned = true;
        world.getPhysicWorld().addBody(body);
        eventBus.publish(new ShipSpawnEvent(this));
    }

    @Override
    public void setRotation(float sin, float cos) {
        super.setRotation(sin, cos);

        if (!spawned) {
            setJumpPosition();
        }
    }

    private void setJumpPosition() {
        RotationHelper.angleToVelocity(sin, cos, -100.0f, jumpPosition);
        jumpPosition.add(position);
    }

    public void setDestroying() {
        if (destroyingTimer == 0) {
            destroyingTimer = timeToDestroy;
            eventBus.publish(new ShipDestroyingEvent(this));
        }
    }

    public boolean isDestroying() {
        return destroyingTimer != 0;
    }

    @Override
    public void setDead() {
        super.setDead();
        eventBus.publish(new ShipDestroyEvent(this));
    }

    public boolean isBot() {
        return owner == null;
    }
}