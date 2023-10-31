package net.bfsr.entity.ship;

import clipper2.core.PathsD;
import gnu.trove.set.hash.THashSet;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiAttackTarget;
import net.bfsr.ai.task.AiSearchTarget;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.hull.Hull;
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
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.Force;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.math.RigidBodyUtils.ANGLE_TO_VELOCITY;

public class Ship extends RigidBody implements Damageable {
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
    private boolean spawned;
    @Getter
    private final int jumpTime = (int) (0.6f * TimeUtils.UPDATES_PER_SECOND);
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
    private RigidBody lastAttacker;

    @Getter
    private final THashSet<Direction> moveDirections = new THashSet<>();
    @Getter
    @Setter
    private PathsD contours = new PathsD();
    @Getter
    private final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    @Getter
    private Ai ai;
    @Getter
    @Setter
    private RigidBody target;
    @Getter
    private DamageMask mask;
    @Getter
    private final ShipData shipData;

    public Ship(ShipData shipData) {
        super(shipData.getSize().x, shipData.getSize().y);
        this.shipData = shipData;
        this.timeToDestroy = shipData.getDestroyTimeInTicks();
        this.maxSparksTimer = timeToDestroy / 3;
        this.jumpTimer = jumpTime;
        setJumpPosition();
    }

    @Override
    public int getDataIndex() {
        return shipData.getDataIndex();
    }

    @Override
    public void init(World world, int id) {
        super.init(world, id);

        if (SideUtils.IS_SERVER && world.isServer()) {
            addAI();
        }

        eventBus = world.getEventBus();
    }

    @Override
    protected void initBody() {
        contours.add(shipData.getContour());

        mask = new DamageMask(128, 128);

        List<Convex> convexes = shipData.getConvexList();
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
            body.getLinearVelocity().multiply(modules.getEngine().getManeuverability() * 0.98f);
            return;
        }

        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();

        if (dir == Direction.FORWARD) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(cos, sin), modules.getEngine().getForwardAcceleration());
        } else if (dir == Direction.BACKWARD) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(-cos, -sin), modules.getEngine().getBackwardAcceleration());
        } else if (dir == Direction.LEFT) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(sin, -cos), modules.getEngine().getSideAcceleration());
        } else if (dir == Direction.RIGHT) {
            addForce(RigidBodyUtils.ROTATE_TO_VECTOR.set(-sin, cos), modules.getEngine().getSideAcceleration());
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
    public void collision(Body body, float contactX, float contactY, float normalX, float normalY,
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
                    collisionTimer = 2 * TimeUtils.UPDATES_PER_SECOND;
                    onCollidedWithWreck(contactX, contactY, normalX, normalY);
                }
            }
        }
    }

    private void onCollidedWithWreck(float contactX, float contactY, float normalX, float normalY) {
        eventBus.publish(new ShipCollisionWithWreckEvent(this, contactX, contactY, normalX, normalY));
    }

    @Override
    public void update() {
        super.update();

        if (spawned) {
            updateShip();
        } else {
            updateJump();
        }
    }

    private void updateShip() {
        if (collisionTimer > 0) collisionTimer -= 1;

        if (destroyingTimer > 0) {
            sparksTimer -= 1;
            if (sparksTimer <= 0) {
                spawnSmallExplosion();
                sparksTimer = maxSparksTimer;
            }

            if (SideUtils.IS_SERVER && world.isServer()) {
                destroyingTimer -= 1;
                if (destroyingTimer <= 0) {
                    setDead();
                }
            }
        } else {
            if (fixturesToAdd.size() > 0) {
                body.removeAllFixtures();
                for (int i = 0; i < fixturesToAdd.size(); i++) {
                    body.addFixture(fixturesToAdd.get(i));
                }
                fixturesToAdd.clear();
            }

            if (ai != null) {
                ai.update();
            }
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
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();

        float maxForwardSpeed = modules.getEngine().getMaxForwardVelocity();
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

    public void shoot() {
        modules.shoot();
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

        collisionTimer = 2 * TimeUtils.UPDATES_PER_SECOND;

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
        if (SideUtils.IS_SERVER && world.isServer()) {
            if (modules.getHull().badlyDamaged()) {
                setDestroying();
            }
        } else {
            eventBus.publish(new ShipHullDamageByCollisionEvent(this, contactX, contactY, normalX, normalY));
        }
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, float contactX, float contactY, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getShield() * multiplayer;

        Shield shield = modules.getShield();
        if (shield != null && shield.damage(shieldDamage)) {
            return false;
        }

        float hullDamage = damage.getHull() * multiplayer;
        float armorDamage = damage.getArmor() * multiplayer;

        float reducedHullDamage = modules.getArmor().reduceDamageByArmor(armorDamage, hullDamage, contactX, contactY, this);
        if (reducedHullDamage == armorDamage) {
            modules.getHull().damage(reducedHullDamage, contactX, contactY, this);
            onHullDamage(contactX, contactY);
        }

        return true;
    }

    private void onHullDamage(float contactX, float contactY) {
        if (SideUtils.IS_SERVER && world.isServer()) {
            if (modules.getHull().badlyDamaged()) {
                setDestroying();
            } else {
                eventBus.publish(new ShipHullDamageEvent(this, contactX, contactY));
            }
        }
    }

    @Override
    public BodyFixture setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        return bodyFixture;
    }

    private void spawnSmallExplosion() {
        eventBus.publish(new ShipDestroyingExplosionEvent(this));
    }

    public void setHull(Hull hull) {
        modules.setHull(hull);
    }

    protected void setCrew(Crew crew) {
        modules.setCrew(crew);
    }

    public void setShield(Shield shield) {
        shield.init(this);
        modules.setShield(shield);
    }

    public void setEngine(Engine engine) {
        modules.setEngine(engine);
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
        return shipData.getWeaponSlotPositions()[id];
    }

    public void addWeaponToSlot(int id, WeaponSlot slot) {
        modules.addWeaponToSlot(id, slot, this);
    }

    public WeaponSlot getWeaponSlot(int i) {
        return modules.getWeaponSlot(i);
    }

    public void setSpawned() {
        spawned = true;
        world.spawnShip(this);
    }

    private void setVelocity(float x, float y) {
        body.setLinearVelocity(x, y);
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

    @Override
    public void updateClientPositionFromPacket(Vector2f position, float sin, float cos, Vector2f velocity,
                                               float angularVelocity) {
        super.updateClientPositionFromPacket(position, sin, cos, velocity, angularVelocity);
        modules.updateWeaponSlotPositions();
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float sin, float cos, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, sin, cos, velocity, angularVelocity);
        modules.updateWeaponSlotPositions();
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

    @Override
    public void clear() {
        modules.clear();
    }

    public void removeAI() {
        this.ai = null;
    }

    public void addAI() {
        this.ai = new Ai();
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
    }
}