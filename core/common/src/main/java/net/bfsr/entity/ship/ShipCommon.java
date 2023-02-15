package net.bfsr.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.component.Armor;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.component.weapon.WeaponSlotBeamCommon;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class ShipCommon extends CollisionObject implements TOITransformSavable {
    @Getter
    private Armor armor;
    @Getter
    protected ShieldCommon shield;
    @Getter
    protected Engine engine;
    @Getter
    @Setter
    private Faction faction;
    @Getter
    private Reactor reactor;
    @Getter
    private Crew crew;
    @Getter
    protected Hull hull;
    @Getter
    @Setter
    private Cargo cargo;

    @Getter
    @Setter
    protected String name;

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    @Getter
    protected List<WeaponSlotCommon> weaponSlots;
    protected boolean spawned;
    protected final Vector2f jumpVelocity = new Vector2f();
    protected final Vector2f jumpPosition = new Vector2f();
    protected final float jumpSpeed = 25.0f;
    protected final Vector3f effectsColor = new Vector3f();
    private int collisionTimer;
    protected int destroyingTimer;
    protected int sparksTimer;
    protected int maxDestroyingTimer, maxSparksTimer;

    protected boolean controlledByPlayer;

    @Getter
    private CollisionObject lastAttacker, target;

    /**
     * Saved transform before TOI solver
     */
    private final Transform savedTransform = new Transform();
    private boolean transformSaved;

    protected ShipCommon(World world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, boolean spawned) {
        super(world, id, x, y, rotation, scaleX, scaleY, r, g, b, 0.0f);
        if (spawned) setSpawned();
    }

    public abstract void init();

    protected void updateShip() {
        Vector2f position = getPosition();
        lastRotation = getRotation();
        lastPosition.set(position.x, position.y);

        if (collisionTimer > 0) collisionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
    }

    private void move(Vector2 r, float speed) {
        Vector2 f = r.product(speed);
        body.applyForce(f);
    }

    public void move(ShipCommon ship, Direction dir) {
        Engine engine = ship.getEngine();
        Vector2 r = new Vector2(body.getTransform().getRotationAngle());
        Vector2f pos = getPosition();

        switch (dir) {
            case FORWARD:
                move(r, engine.getForwardSpeed());
                break;
            case BACKWARD:
                r.negate();
                move(r, engine.getBackwardSpeed());
                break;
            case LEFT:
                r.left();
                move(r, engine.getSideSpeed());
                break;
            case RIGHT:
                r.right();
                move(r, engine.getSideSpeed());
                break;
            case STOP:
                body.getLinearVelocity().multiply(engine.getManeuverability() / 1.02f);

                float x = -(float) body.getLinearVelocity().x;
                float y = -(float) body.getLinearVelocity().y;

                if (Math.abs(x) > 10) {
                    dir = calculateDirectionToOtherObject(x + pos.x, pos.y);
                    onStopMove(dir);
                }

                if (Math.abs(y) > 10) {
                    dir = calculateDirectionToOtherObject(pos.x, y + pos.y);
                    onStopMove(dir);
                }

                return;
        }

        onMove(dir);
    }

    protected abstract void onMove(Direction direction);
    protected abstract void onStopMove(Direction direction);

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof ShipCommon otherShip) {
                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400.0f;

                if (impactPowerForOther > 0.25f) otherShip.damageByCollision(this, impactPowerForOther, contact, normal);
            } else if (userData instanceof WreckCommon) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                    onCollidedWithWreck(contact, normal);
                }
            }
        }
    }

    protected void onCollidedWithWreck(Contact contact, Vector2 normal) {

    }

    public void update() {
        super.update();

        if (spawned) {
            updateShip();
        } else {
            updateJump();
        }
    }

    protected void updateJump() {
        if (((jumpVelocity.x < 0 && jumpPosition.x <= position.x) || (jumpVelocity.x >= 0 && jumpPosition.x >= position.x)) && ((jumpVelocity.y < 0 && jumpPosition.y <= position.y)
                || (jumpVelocity.y >= 0 && jumpPosition.y >= position.y))) {
            setSpawned();
            setVelocity(jumpVelocity.mul(0.26666668f));
            onShipSpawned();
        } else {
            jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
            color.w += 1.5f * TimeUtils.UPDATE_DELTA_TIME;
        }
    }

    protected void onShipSpawned() {

    }

    @Override
    public void postPhysicsUpdate() {
        if (transformSaved) {
            body.setTransform(savedTransform);
            transformSaved = false;
        }

        super.postPhysicsUpdate();

        float maxForwardSpeed = engine.getMaxForwardSpeed();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        double magnitudeSquared = body.getLinearVelocity().getMagnitudeSquared();
        if (magnitudeSquared > maxForwardSpeedSquared) {
            double percent = maxForwardSpeedSquared / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        }

        updateComponents();
    }

    @Override
    public void saveTransform(Transform transform) {
        this.savedTransform.set(transform);
        transformSaved = true;
    }

    protected void shoot() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.tryShoot();
        }
    }

    protected void updateComponents() {
        if (shield != null) shield.update();
        if (armor != null) armor.update();
        if (reactor != null) reactor.update();
        if (hull != null) hull.update();

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.update();
        }
    }

    private void damageByCollision(ShipCommon otherShip, float impactPower, Contact contact, Vector2 normal) {
        if (collisionTimer > 0) {
            return;
        }

        lastAttacker = otherShip;
        if (otherShip.faction == faction) {
            impactPower /= 2.0f;
        }

        collisionTimer = 2;

        if (shield != null && shield.damage(impactPower)) {
            onShieldDamageByCollision(contact, normal);
            return;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;
        Vector2f otherPos = otherShip.getPosition();
        Direction dir = calculateDirectionToOtherObject(otherPos.x, otherPos.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamageByCollision(contact, normal);
    }

    protected void onShieldDamageByCollision(Contact contact, Vector2 normal) {

    }

    protected void onHullDamageByCollision(Contact contact, Vector2 normal) {

    }

    public boolean attackShip(BulletDamage damage, ShipCommon attacker, Vector2f contactPoint, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getBulletDamageShield() * multiplayer;

        if ((shield != null && shield.damage(shieldDamage))) {
            return false;
        }

        float hullDamage = damage.getBulletDamageHull() * multiplayer;
        float armorDamage = damage.getBulletDamageArmor() * multiplayer;
        Direction dir = calculateDirectionToOtherObject(contactPoint.x, contactPoint.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamage();
        return true;
    }

    protected void onHullDamage() {

    }

    public void setDestroying() {
        if (destroyingTimer == 0) destroyingTimer = maxDestroyingTimer;
    }

    public boolean isDestroying() {
        return destroyingTimer != 0;
    }

    protected abstract void createSpark();

    protected abstract void createDestroyParticles();

    public void destroyShip() {
        createDestroyParticles();
    }

    public void setHull(Hull hull) {
        this.hull = hull;
    }

    protected void setCrew(Crew crew) {
        this.crew = crew;
    }

    protected void createWeaponPosition(Vector2f pos) {
        weaponPositions.add(pos);
    }

    private Vector2f getWeaponSlotPosition(int i) {
        return weaponPositions.get(i);
    }

    protected void setWeaponsCount(int count) {
        weaponSlots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            weaponSlots.add(null);
        }
    }

    public void addWeaponToSlot(int i, WeaponSlotCommon slot) {
        if (i < weaponSlots.size()) {
            WeaponSlotCommon oldSlot = weaponSlots.get(i);
            if (oldSlot != null) {
                oldSlot.clear();
            }
        }

        slot.init(i, getWeaponSlotPosition(i), this);

        weaponSlots.set(i, slot);
    }

    public void recalculateMass() {
        body.setMass(MassType.NORMAL);
    }

    public WeaponSlotCommon getWeaponSlot(int i) {
        return weaponSlots.get(i);
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setShield(ShieldCommon shield) {
        this.shield = shield;
    }

    public void spawnEngineParticles(Direction dir) {

    }

    public void setSpawned() {
        color.w = 1.0f;
        spawned = true;
        world.spawnShip(this);
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setVelocity(Vector2f velocity) {
        body.setLinearVelocity(new Vector2(velocity.x, velocity.y));
    }

    public void setRotation(float rotation) {
        body.getTransform().setRotation(rotation);
    }

    public Vector3f getEffectsColor() {
        return effectsColor;
    }

    public void clear() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon slot = weaponSlots.get(i);
            if (slot instanceof WeaponSlotBeamCommon) slot.clear();
        }
    }

    public void setControlledByPlayer(boolean controlledByPlayer) {
        this.controlledByPlayer = controlledByPlayer;
    }

    public boolean isControlledByPlayer() {
        return controlledByPlayer;
    }

    public void setTarget(CollisionObject target) {
        this.target = target;
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);

        sin = (float) body.getTransform().getRotation().getSint();
        cos = (float) body.getTransform().getRotation().getCost();

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }

    public abstract ShipType getType();
}