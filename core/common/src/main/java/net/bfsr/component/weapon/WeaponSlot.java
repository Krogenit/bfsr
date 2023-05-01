package net.bfsr.component.weapon;

import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.entity.bullet.BulletData;
import net.bfsr.config.entity.bullet.BulletRegistry;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.EventBus;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;

import java.util.List;

public class WeaponSlot extends GameObject {
    @Getter
    protected int id;
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
    private final WeaponType type;
    @Getter
    private final BulletData bulletData;

    public WeaponSlot(GunData gunData, WeaponType type) {
        super(gunData.getSizeX(), gunData.getSizeY());
        this.timeToReload = (int) (gunData.getReloadTimeInSeconds() * TimeUtils.UPDATES_PER_SECOND);
        this.energyCost = gunData.getEnergyCost();
        this.type = type;
        this.polygon = gunData.getPolygon();
        this.bulletData = BulletRegistry.INSTANCE.get(gunData.getBulletData());
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
        createBody();
        updatePos();
    }

    private void createBody() {
        Polygon polygon = Geometry.createPolygon(this.polygon.getVertices());
        polygon.translate(localPosition.x, localPosition.y);
        BodyFixture bodyFixture = new BodyFixture(polygon);
        bodyFixture.setUserData(this);
        bodyFixture.setFilter(new ShipFilter(ship));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        ship.getBody().addFixture(bodyFixture);
        ship.getBody().updateMass();
    }

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (reloadTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    public void shoot() {
        reloadTimer = timeToReload;
        ship.getReactor().consume(energyCost);
        EventBus.post(world.getSide(), new WeaponShotEvent(this));
    }

    public void createBullet() {
        Bullet bullet = new Bullet(position.x, position.y, ship.getSin(), ship.getCos(), ship, bulletData);
        bullet.init(world, world.getNextId());
        world.addBullet(bullet);
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
    public void clear() {
        Body shipBody = ship.getBody();
        List<BodyFixture> bodyFixtures = shipBody.getFixtures();
        for (int i = 0, bodyFixturesSize = bodyFixtures.size(); i < bodyFixturesSize; i++) {
            BodyFixture bodyFixture = bodyFixtures.get(i);
            Object userData = bodyFixture.getUserData();
            if (userData == this) {
                shipBody.removeFixture(bodyFixture);
                shipBody.updateMass();
                break;
            }
        }
    }
}