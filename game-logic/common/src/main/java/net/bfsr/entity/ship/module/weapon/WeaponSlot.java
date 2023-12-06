package net.bfsr.entity.ship.module.weapon;

import clipper2.core.PathD;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.event.module.weapon.WeaponShotEvent;
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

public class WeaponSlot extends DamageableModule implements ConnectedObject {
    protected World world;
    @Getter
    protected Ship ship;
    protected float energyCost;
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
        this.ship = ship;
        this.world = ship.getWorld();
        this.localPosition = ship.getWeaponSlotPosition(id);
        this.polygon.translate(localPosition.x, localPosition.y);
        this.eventBus = world.getEventBus();
        init(ship);
        updatePos();
    }

    @Override
    public void spawn() {
        if (SideUtils.IS_SERVER && world.isServer()) {
            RigidBody<GunData> rigidBody = new RigidBody<>(position.x, position.y,
                    ship.getSin(), ship.getCos(), gunData.getSizeX(), gunData.getSizeY(), gunData,
                    weaponType == WeaponType.GUN ? GunRegistry.INSTANCE.getId() : BeamRegistry.INSTANCE.getId());
            rigidBody.setHealth(5.0f);

            Polygon polygon = Geometry.createPolygon(this.polygon.getVertices());
            BodyFixture fixture = new BodyFixture(polygon);
            fixture.setUserData(this);
            fixture.setFilter(new ShipFilter(rigidBody));
            fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
            Body body = rigidBody.getBody();
            body.addFixture(fixture);
            rigidBody.init(world, world.getNextId());
            body.setMass(MassType.NORMAL);
            body.setUserData(rigidBody);
            body.setLinearDamping(0.05f);
            body.setAngularDamping(0.005f);
            body.setLinearVelocity(ship.getBody().getLinearVelocity());
            body.setAngularVelocity(ship.getBody().getAngularVelocity());

            world.add(rigidBody);
        }
    }

    @Override
    protected void createFixture() {
        fixture = new BodyFixture(polygon);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(ship));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        ship.getBody().addFixture(fixture);
    }

    @Override
    public void addFixtures(Body body) {
        body.addFixture(fixture);
    }

    public void tryShoot() {
        float energy = ship.getModules().getReactor().getEnergy();
        if (reloadTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    public void shoot() {
        reloadTimer = timeToReload;
        ship.getModules().getReactor().consume(energyCost);
        eventBus.publish(new WeaponShotEvent(this));
    }

    public void createBullet() {
        Bullet bullet = new Bullet(position.x, position.y, ship.getSin(), ship.getCos(), gunData, ship,
                gunData.getDamage().copy());
        bullet.init(world, world.getNextId());
        world.add(bullet);
    }

    @Override
    public void update() {
        updatePos();
        if (reloadTimer > 0) {
            reloadTimer -= 1;
        }
    }

    public void updatePos() {
        Vector2f shipPos = ship.getPosition();
        float x = localPosition.x;
        float y = localPosition.y;
        float cos = ship.getCos();
        float sin = ship.getSin();
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

    @Override
    public void writeData(ByteBuf data) {
        data.writeInt(weaponType == WeaponType.GUN ? GunRegistry.INSTANCE.getId() : BeamRegistry.INSTANCE.getId());
        data.writeInt(gunData.getId());
        ByteBufUtils.writeVector(data, localPosition);
    }

    @Override
    public void readData(ByteBuf data) {}

    @Override
    public boolean isInside(PathD contour) {
        return DamageSystem.isPolygonConnectedToContour(this.polygon.getVertices(), contour);
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
}