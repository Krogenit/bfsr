package net.bfsr.server.entity.ship;

import clipper2.core.Path64;
import clipper2.core.PathsD;
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
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.ai.Ai;
import net.bfsr.server.ai.AiAggressiveType;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.collision.filter.ShipFilter;
import net.bfsr.server.component.Shield;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.core.Server;
import net.bfsr.server.damage.Damagable;
import net.bfsr.server.damage.DamageMask;
import net.bfsr.server.damage.DamageUtils;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.common.PacketShipEngine;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.server.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.server.network.packet.server.entity.ship.PacketSpawnShip;
import net.bfsr.server.player.Player;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends CollisionObject implements Damagable {
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
    @Setter
    protected String name;

    @Getter
    protected final List<WeaponSlot> weaponSlots = new ArrayList<>();
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

    @Getter
    @Setter
    private Player owner;
    @Getter
    private final Ai ai;
    @Getter
    private Direction lastMoveDir = Direction.STOP;
    @Getter
    @Setter
    protected PathsD contours = new PathsD();
    @Getter
    protected DamageMask mask;
    @Getter
    protected List<BodyFixture> fixturesToAdd = new ArrayList<>();
    @Getter
    private final int textureIndex;

    protected Ship(float scaleX, float scaleY, int textureIndex) {
        super(scaleX, scaleY);
        this.ai = new Ai(this);
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
        this.textureIndex = textureIndex;
    }

    public void init(WorldServer world) {
        this.world = world;
        this.id = world.getNextId();

        if (!spawned) {
            RotationHelper.angleToVelocity(rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
            jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + position.x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + position.y);
        }

        super.init();

        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            weaponSlot.init(i, getWeaponSlotPosition(i), this);
        }

        this.world.addShip(this);
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Use init(WorldServer) instead!");
    }

    @Override
    public void setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
    }

    public void sendSpawnPacket() {
        Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

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
                destroy();
            }
        } else {
            if (fixturesToAdd.size() > 0) {
                body.removeAllFixtures();
                for (int i = 0; i < fixturesToAdd.size(); i++) {
                    body.addFixture(fixturesToAdd.get(i));
                }
                fixturesToAdd.clear();
            }
        }
    }

    private void move(Vector2 r, float speed) {
        Vector2 f = r.product(speed);
        body.applyForce(f);
    }

    public void move(Direction dir) {
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();
        Vector2 r = new Vector2(cos, sin);
        Vector2f pos = getPosition();

        if (dir == Direction.FORWARD) {
            move(r, engine.getForwardSpeed());
        } else if (dir == Direction.BACKWARD) {
            r.negate();
            move(r, engine.getBackwardSpeed());
        } else if (dir == Direction.LEFT) {
            r.left();
            move(r, engine.getSideSpeed());
        } else if (dir == Direction.RIGHT) {
            r.right();
            move(r, engine.getSideSpeed());
        } else if (dir == Direction.STOP) {
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
            Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
        lastMoveDir = direction;
    }

    private void onStopMove(Direction direction) {
        Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
    }

    public void collision(Body body, float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
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
        if ((jumpVelocity.x < 0 && jumpPosition.x <= position.x || jumpVelocity.x >= 0 && jumpPosition.x >= position.x) && (jumpVelocity.y < 0 && jumpPosition.y <= position.y
                || jumpVelocity.y >= 0 && jumpPosition.y >= position.y)) {
            setSpawned();
            setVelocity(jumpVelocity.mul(0.26666668f));
        } else {
            jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
        }
    }

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();

        float maxForwardSpeed = engine.getMaxForwardSpeed();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        double magnitudeSquared = body.getLinearVelocity().getMagnitudeSquared();
        if (magnitudeSquared > maxForwardSpeedSquared) {
            double percent = maxForwardSpeedSquared / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        }

        updateComponents();

        Player player = world.getPlayer(name);
        if (controlledByPlayer) {
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearbyExcept(new PacketObjectPosition(this), position, WorldServer.PACKET_UPDATE_DISTANCE, player);
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        } else {
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketObjectPosition(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        }
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
            Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketDestroyingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, float contactX, float contactY, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getBulletDamageShield() * multiplayer;

        if ((shield != null && shield.damage(shieldDamage))) {
            return false;
        }

        float hullDamage = damage.getBulletDamageHull() * multiplayer;
        float armorDamage = damage.getBulletDamageArmor() * multiplayer;
        Direction dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, contactX, contactY);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamage(contactX, contactY);
        return true;
    }

    protected void onHullDamage(float contactX, float contactY) {
        if (hull.getHull() <= 0) {
            setDestroying();
            Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketDestroyingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        } else {
            float polygonRadius = 0.75f;
            float radius = 2.0f;

            double x = body.getTransform().getTranslationX();
            double y = body.getTransform().getTranslationY();
            double sin = body.getTransform().getSint();
            double cos = body.getTransform().getCost();

            Path64 clip = DamageUtils.createCirclePath(contactX - x, contactY - y, -sin, cos, 12, polygonRadius);

            DamageUtils.damage(this, contactX, contactY, clip, radius);
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

    @Override
    public void destroy() {
        setDead(true);
        createDestroyParticles();
        Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public void setHull(Hull hull) {
        this.hull = hull;
    }

    protected void setCrew(Crew crew) {
        this.crew = crew;
    }

    protected abstract Vector2f getWeaponSlotPosition(int id);

    public void addWeaponToSlot(int id, WeaponSlot slot) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot.getId() == id) {
                weaponSlot.clear();
                weaponSlots.set(i, slot);
                slot.init(id, getWeaponSlotPosition(id), this);
                return;
            }
        }

        slot.init(id, getWeaponSlotPosition(id), this);
        weaponSlots.add(slot);
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
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float angle, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, angle, velocity, angularVelocity);

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
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