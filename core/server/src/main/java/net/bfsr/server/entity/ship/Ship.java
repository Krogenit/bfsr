package net.bfsr.server.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.component.Armor;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.MainServer;
import net.bfsr.server.ai.Ai;
import net.bfsr.server.ai.AiAggressiveType;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.component.Shield;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.common.PacketShipEngine;
import net.bfsr.server.network.packet.server.*;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends CollisionObject implements TOITransformSavable {
    @Getter
    @Setter
    private Armor armor;
    @Getter
    @Setter
    protected Shield shield;
    @Getter
    @Setter
    protected Engine engine;
    @Getter
    @Setter
    private Faction faction;
    @Getter
    @Setter
    private Reactor reactor;
    @Getter
    private Crew crew;
    @Getter
    protected Hull hull;
    @Getter
    @Setter
    private Cargo cargo;

    @Getter
    protected String name;

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    @Getter
    protected List<WeaponSlot> weaponSlots;
    protected boolean spawned;
    protected final Vector2f jumpVelocity = new Vector2f();
    protected final Vector2f jumpPosition = new Vector2f();
    protected final float jumpSpeed = 25.0f;
    private int collisionTimer;
    protected int destroyingTimer;
    protected int sparksTimer;
    protected int maxDestroyingTimer, maxSparksTimer;

    @Getter
    @Setter
    protected boolean controlledByPlayer;

    @Getter
    private CollisionObject lastAttacker;
    @Getter
    @Setter
    private CollisionObject target;

    /**
     * Saved transform before TOI solver
     */
    private final Transform savedTransform = new Transform();
    private boolean transformSaved;

    @Getter
    @Setter
    private PlayerServer owner;
    @Getter
    private final Ai ai;
    @Getter
    private Direction lastMoveDir = Direction.STOP;

    protected Ship(WorldServer world, float x, float y, float rotation, float scaleX, float scaleY, boolean spawned) {
        super(world, world.getNextId(), x, y, rotation, scaleX, scaleY);
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        setRotation(this.rotation);
        if (spawned) setSpawned();
        this.ai = new Ai(this);
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
        world.addShip(this);
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public abstract void init();

    protected void updateShip() {
        if (collisionTimer > 0) collisionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;

        if (!controlledByPlayer && destroyingTimer == 0 && ai != null) {
            ai.update();
        }

        if (destroyingTimer > 0) {
            sparksTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (sparksTimer <= 0) {
                createSpark();
                sparksTimer = 25;
            }

            destroyingTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (destroyingTimer <= 0) {
                destroyShip();
            }
        }
    }

    private void move(Vector2 r, float speed) {
        Vector2 f = r.product(speed);
        body.applyForce(f);
    }

    public void move(Ship ship, Direction dir) {
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
                    dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, x + pos.x, pos.y);
                    onStopMove(dir);
                }

                if (Math.abs(y) > 10) {
                    dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, pos.x, y + pos.y);
                    onStopMove(dir);
                }

                return;
        }

        onMove(dir);
    }

    private void onMove(Direction direction) {
        if (lastMoveDir != direction)
            MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
        lastMoveDir = direction;
    }

    private void onStopMove(Direction direction) {
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
    }

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship otherShip) {
                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400.0f;

                if (impactPowerForOther > 0.25f) otherShip.damageByCollision(this, impactPowerForOther);
            } else if (userData instanceof Wreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                }
            }
        }
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
        } else {
            jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
        }
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

        PlayerServer player = world.getPlayer(name);
        if (controlledByPlayer) {
            MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearbyExcept(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE, player);
            MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        } else {
            MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE);
            MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        }
    }

    @Override
    public void saveTransform(Transform transform) {
        this.savedTransform.set(transform);
        transformSaved = true;
    }

    protected void updateComponents() {
        if (shield != null) shield.update();
        if (armor != null) armor.update();
        if (reactor != null) reactor.update();
        if (hull != null) hull.regenHull(crew.getCrewRegen());

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.update();
        }
    }

    private void damageByCollision(Ship otherShip, float impactPower) {
        if (collisionTimer > 0) {
            return;
        }

        lastAttacker = otherShip;
        if (otherShip.faction == faction) {
            impactPower /= 2.0f;
        }

        collisionTimer = 2;

        if (shield != null && shield.damage(impactPower)) {
            return;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;
        Vector2f otherPos = otherShip.getPosition();
        Direction dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, otherPos.x, otherPos.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamageByCollision();
    }

    protected void onHullDamageByCollision() {
        if (hull.getHull() <= 0) {
            setDestroying();
            MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketDestroyingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, Vector2f contactPoint, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getBulletDamageShield() * multiplayer;

        if ((shield != null && shield.damage(shieldDamage))) {
            return false;
        }

        float hullDamage = damage.getBulletDamageHull() * multiplayer;
        float armorDamage = damage.getBulletDamageArmor() * multiplayer;
        Direction dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, contactPoint.x, contactPoint.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamage();
        return true;
    }

    protected void onHullDamage() {
        if (hull.getHull() <= 0) {
            setDestroying();
            MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketDestroyingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public void setDestroying() {
        if (destroyingTimer == 0) destroyingTimer = maxDestroyingTimer;
    }

    public boolean isDestroying() {
        return destroyingTimer != 0;
    }

    private void createSpark() {
        Random rand = world.getRand();
        Vector2f position = getPosition();
        Vector2f velocity = getVelocity();
        WreckSpawner.spawnDamageDebris(world, 1, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)),
                position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f, 1.0f);
    }

    protected abstract void createDestroyParticles();

    public void destroyShip() {
        setDead(true);
        createDestroyParticles();
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
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

    public void addWeaponToSlot(int i, WeaponSlot slot) {
        if (i < weaponSlots.size()) {
            WeaponSlot oldSlot = weaponSlots.get(i);
            if (oldSlot != null) {
                oldSlot.clear();
            }
        }

        slot.init(i, getWeaponSlotPosition(i), this);

        weaponSlots.set(i, slot);
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipSetWeaponSlot(this, slot), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public void recalculateMass() {
        body.setMass(MassType.NORMAL);
    }

    public WeaponSlot getWeaponSlot(int i) {
        return weaponSlots.get(i);
    }

    public void setSpawned() {
        spawned = true;
        world.spawnShip(this);
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setVelocity(Vector2f velocity) {
        body.setLinearVelocity(new Vector2(velocity.x, velocity.y));
    }

    @Override
    public void setRotation(float rotation) {
        body.getTransform().setRotation(rotation);
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }

    public void setName(String name) {
        this.name = name;
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipName(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipFaction(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public boolean isBot() {
        return owner == null;
    }

    public abstract ShipType getType();

    @Override
    public void clear() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot != null) slot.clear();
        }
    }
}