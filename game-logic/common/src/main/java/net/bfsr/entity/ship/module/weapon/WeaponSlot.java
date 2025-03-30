package net.bfsr.entity.ship.module.weapon;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.ConnectedObjectType;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.EntityDataHistoryManager;
import net.bfsr.engine.entity.TransformData;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.physics.PhysicsUtils;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.physics.collision.filter.Filters;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class WeaponSlot extends DamageableModule implements ConnectedObject<GunData> {
    protected World world;
    private final float energyCost;
    @Getter
    protected int reloadTimer, timeToReload;
    @Getter
    @Setter
    protected Vector2f localPosition;
    private final Vector2f connectionOffset = new Vector2f();
    private final Polygon polygon;
    @Getter
    private final GunData gunData;
    @Getter
    private final WeaponType weaponType;
    protected EventBus eventBus;
    @Getter
    private float sin, cos;
    @Getter
    protected final EventBus weaponSlotEventBus = new EventBus();

    public WeaponSlot(GunData gunData, WeaponType weaponType) {
        super(gunData, gunData.getHp(), gunData.getSizeX(), gunData.getSizeY());
        this.timeToReload = gunData.getReloadTimeInTicks();
        this.energyCost = gunData.getEnergyCost();
        this.weaponType = weaponType;
        this.polygon = new Polygon(gunData.getPolygon().getVertices());
        this.gunData = gunData;
    }

    public WeaponSlot(GunData gunData) {
        this(gunData, WeaponType.GUN);
    }

    public void init(int id, Ship ship) {
        this.id = id;
        this.world = ship.getWorld();
        this.localPosition = ship.getWeaponSlotPosition(id);
        this.polygon.translate(localPosition.x, localPosition.y);
        this.eventBus = world.getEventBus();
        init(ship);
        updatePos(ship);
    }

    @Override
    public void init(RigidBody rigidBody) {
        polygon.translate(localPosition.x, localPosition.y);
        createFixture(rigidBody);
        updatePos(rigidBody);
    }

    @Override
    public void spawn() {
        if (world.isClient()) {
            return;
        }

        RigidBody rigidBody = new RigidBody(getX(), getY(), this.ship.getSin(), this.ship.getCos(),
                gunData.getSizeX(), gunData.getSizeY(), gunData);
        rigidBody.setHealth(5.0f);
        rigidBody.init(world, world.getNextId());
        Body body = rigidBody.getBody();

        rigidBody.addFixture(new Fixture(new Polygon(gunData.getPolygon().getVertices()), Filters.SHIP_FILTER, this,
                PhysicsUtils.DEFAULT_FIXTURE_DENSITY));
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
        body.setLinearVelocity(this.ship.getBody().getLinearVelocity());
        body.setAngularVelocity(this.ship.getBody().getAngularVelocity());

        world.add(rigidBody);
    }

    @Override
    protected void createFixture(RigidBody rigidBody) {
        fixture = new Fixture(polygon, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.addFixture(fixture);
    }

    @Override
    public void addFixtures(DamageableRigidBody rigidBody) {
        rigidBody.addFixture(fixture);
    }

    public void tryShoot(Consumer<WeaponSlot> onShotConsumer, Reactor reactor) {
        float energy = reactor.getEnergy();
        if (reloadTimer <= 0 && energy >= energyCost) {
            shoot(onShotConsumer, reactor);
        }
    }

    public void shoot(Consumer<WeaponSlot> onShotConsumer, Reactor reactor) {
        reloadTimer = timeToReload;
        reactor.consume(energyCost);
        onShotConsumer.accept(this);
        weaponSlotEventBus.publish(new WeaponShotEvent(this));
    }

    public void createBullet(float fastForwardTimeInMillis) {
        float cos = ship.getCos();
        float sin = ship.getSin();
        float x = getX() + getSizeX() * cos;
        float y = getY() + getSizeX() * sin;

        Bullet bullet = new Bullet(x, y, sin, cos, gunData, ship, gunData.getDamage().copy());
        bullet.init(world, world.getNextId());
        world.add(bullet);

        float updateDeltaTimeInMills = Engine.getUpdateDeltaTime() * 1000.0f;
        int iterations = Math.round(fastForwardTimeInMillis / updateDeltaTimeInMills);
        if (iterations > 0) {
            float bulletSpeed = gunData.getBulletSpeed();
            float offset = 1.0f + Math.max(gunData.getBulletSizeX(), gunData.getBulletSizeY());
            float endX = x + cos * bulletSpeed;
            float endY = y + sin * bulletSpeed;
            float minX = Math.min(x, endX) - offset;
            float minY = Math.min(y, endY) - offset;
            float maxX = Math.max(x, endX) + offset;
            float maxY = Math.max(y, endY) + offset;
            AABB aabb = new AABB(new Vector2(minX, minY), new Vector2(maxX, maxY));

            Set<Body> affectedBodies = new HashSet<>();

            Body bulletBody = bullet.getBody();
            org.jbox2d.dynamics.World physicWorld = world.getPhysicWorld();
            physicWorld.queryAABB(fixture -> {
                Body body = fixture.getBody();
                if (affectedBodies.contains(body)) {
                    return true;
                }

                if (body == bulletBody) {
                    return true;
                }

                affectedBodies.add(body);
                return true;
            }, aabb);

            EntityDataHistoryManager entityDataHistoryManager = new EntityDataHistoryManager();

            for (Body body : affectedBodies) {
                RigidBody rigidBody = (RigidBody) body.getUserData();
                entityDataHistoryManager.addPositionData(rigidBody.getId(), rigidBody.getX(), rigidBody.getY(),
                        rigidBody.getSin(), rigidBody.getCos(), 0);
            }

            physicWorld.beginFastForward();

            double timestamp = world.getTimestamp();
            float fastForwardTimeInNanos = fastForwardTimeInMillis * 1_000_000.0f;
            float updateDeltaTimeInNanos = updateDeltaTimeInMills * 1_000_000.0f;
            for (int i = 0; i < iterations; i++) {
                EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

                for (Body body : affectedBodies) {
                    RigidBody rigidBody = (RigidBody) body.getUserData();
                    TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(),
                            timestamp - fastForwardTimeInNanos);
                    if (transformData != null) {
                        Vector2f position = transformData.getPosition();
                        body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
                    }
                }

                bullet.update();
                physicWorld.fastForwardStep(Engine.getUpdateDeltaTime(), Collections.singletonList(bulletBody));
                bullet.postPhysicsUpdate();

                fastForwardTimeInNanos -= updateDeltaTimeInNanos;
            }

            physicWorld.endFastForward();
            for (Body body : affectedBodies) {
                RigidBody rigidBody = (RigidBody) body.getUserData();
                TransformData transformData = entityDataHistoryManager.getFirstTransformData(rigidBody.getId());
                Vector2f position = transformData.getPosition();
                body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
            }
        }
    }

    @Override
    public void update() {
        if (reloadTimer > 0) {
            reloadTimer -= 1;
        }
    }

    @Override
    public void postPhysicsUpdate(RigidBody rigidBody) {
        updatePos(rigidBody);
    }

    void updatePos(RigidBody rigidBody) {
        float x = localPosition.x;
        float y = localPosition.y;
        cos = rigidBody.getCos();
        sin = rigidBody.getSin();
        float xPos = cos * x - sin * y;
        float yPos = sin * x + cos * y;
        setPosition(xPos + rigidBody.getX(), yPos + rigidBody.getY());
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.removeConnectedObject(this);
        spawn();
    }

    public void removeFixture() {
        Body shipBody = ship.getBody();
        for (int i = 0; i < shipBody.fixtures.size(); i++) {
            Fixture fixture = shipBody.fixtures.get(i);
            Object userData = fixture.getUserData();
            if (userData == this) {
                shipBody.removeFixture(fixture);
                break;
            }
        }
    }

    public void onRemoved() {
        WeaponSlotRemovedEvent event = new WeaponSlotRemovedEvent(this);
        eventBus.publish(event);
        weaponSlotEventBus.publish(event);
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeInt(id);
        data.writeFloat(connectionOffset.x + localPosition.x);
        data.writeFloat(connectionOffset.y + localPosition.y);
    }

    @Override
    public void addPositionOffset(float x, float y) {
        connectionOffset.set(x, y);
    }

    @Override
    public boolean isInside(org.locationtech.jts.geom.Polygon polygon, float offsetX, float offsetY) {
        return DamageSystem.isPolygonConnectedToContour(this.polygon.getVertices(), polygon, offsetX, offsetY);
    }

    @Override
    public ModuleType getType() {
        return ModuleType.WEAPON_SLOT;
    }

    @Override
    public float getConnectPointX() {
        return localPosition.x;
    }

    @Override
    public float getConnectPointY() {
        return localPosition.y;
    }

    @Override
    public ConnectedObjectType getConnectedObjectType() {
        return ConnectedObjectType.WEAPON_SLOT;
    }

    @Override
    public int getRegistryId() {
        return data.getRegistryId();
    }

    @Override
    public GunData getConfigData() {
        return gunData;
    }
}