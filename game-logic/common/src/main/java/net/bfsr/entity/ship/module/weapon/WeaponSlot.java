package net.bfsr.entity.ship.module.weapon;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.ConnectedObjectType;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Consumer;

public class WeaponSlot extends DamageableModule implements ConnectedObject<GunData> {
    protected World world;
    float energyCost;
    @Getter
    protected int reloadTimer, timeToReload;
    @Getter
    protected Vector2f localPosition;
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
        super(gunData.getHp(), gunData.getSizeX(), gunData.getSizeY());
        this.timeToReload = gunData.getReloadTimeInTicks();
        this.energyCost = gunData.getEnergyCost();
        this.weaponType = weaponType;
        this.polygon = Geometry.createPolygon(gunData.getPolygon().getVertices());
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
        if (world.isClient()) return;

        RigidBody rigidBody = new RigidBody(position.x, position.y, this.ship.getSin(), this.ship.getCos(),
                gunData.getSizeX(), gunData.getSizeY(), gunData, getRegistryId());
        rigidBody.setHealth(5.0f);
        rigidBody.init(world, world.getNextId());

        Polygon polygon = Geometry.createPolygon(this.polygon.getVertices());
        polygon.translate(-localPosition.x, -localPosition.y);
        BodyFixture fixture = new BodyFixture(polygon);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(rigidBody));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        Body body = rigidBody.getBody();
        body.addFixture(fixture);
        body.setMass(MassType.NORMAL);
        body.setUserData(rigidBody);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
        body.setLinearVelocity(this.ship.getBody().getLinearVelocity());
        body.setAngularVelocity(this.ship.getBody().getAngularVelocity());

        world.add(rigidBody);
    }

    @Override
    protected void createFixture(RigidBody rigidBody) {
        fixture = new BodyFixture(polygon);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(rigidBody));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.getBody().addFixture(fixture);
    }

    @Override
    public void addFixtures(Body body) {
        body.addFixture(fixture);
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

    public void createBullet(float fastForwardTime) {
        float cos = ship.getCos();
        float sin = ship.getSin();
        float x = position.x + size.x * cos;
        float y = position.y + size.x * sin;

        float updateDeltaTime = ship.getWorld().getUpdateDeltaTime();
        float updateDeltaTimeInMills = updateDeltaTime * 1000;
        while (fastForwardTime > 0) {
            x += cos * gunData.getBulletSpeed() * updateDeltaTime;
            y += sin * gunData.getBulletSpeed() * updateDeltaTime;
            fastForwardTime -= updateDeltaTimeInMills;
        }

        Bullet bullet = new Bullet(x, y, sin, cos, gunData, ship, gunData.getDamage().copy());
        bullet.init(world, world.getNextId());
        world.add(bullet);
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
        Vector2f shipPos = rigidBody.getPosition();
        float x = localPosition.x;
        float y = localPosition.y;
        cos = rigidBody.getCos();
        sin = rigidBody.getSin();
        float xPos = cos * x - sin * y;
        float yPos = sin * x + cos * y;
        position.set(xPos + shipPos.x, yPos + shipPos.y);
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.removeConnectedObject(this);
        spawn();
    }

    public void removeFixture() {
        Body shipBody = ship.getBody();
        List<BodyFixture> bodyFixtures = shipBody.getFixtures();
        for (int i = 0, bodyFixturesSize = bodyFixtures.size(); i < bodyFixturesSize; i++) {
            BodyFixture bodyFixture = bodyFixtures.get(i);
            Object userData = bodyFixture.getUserData();
            if (userData == this) {
                shipBody.removeFixture(bodyFixture);
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
        ByteBufUtils.writeVector(data, localPosition);
    }

    @Override
    public void readData(ByteBuf data) {
        id = data.readInt();
        ByteBufUtils.readVector(data, localPosition = new Vector2f());
    }

    @Override
    public boolean isInside(org.locationtech.jts.geom.Polygon polygon) {
        return DamageSystem.isPolygonConnectedToContour(this.polygon.getVertices(), polygon);
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
        return GunRegistry.INSTANCE.getId();
    }

    @Override
    public int getDataId() {
        return gunData.getId();
    }

    @Override
    public GunData getConfigData() {
        return gunData;
    }
}