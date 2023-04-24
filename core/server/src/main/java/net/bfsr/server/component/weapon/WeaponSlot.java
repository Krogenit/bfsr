package net.bfsr.server.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.component.weapon.WeaponType;
import net.bfsr.config.bullet.BulletData;
import net.bfsr.config.bullet.BulletRegistry;
import net.bfsr.config.weapon.gun.GunData;
import net.bfsr.config.weapon.gun.GunRegistry;
import net.bfsr.entity.GameObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.collision.filter.ShipFilter;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.bullet.Bullet;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;

import java.util.List;

public class WeaponSlot extends GameObject {
    @Getter
    @Setter
    protected int id;
    protected WorldServer world;
    @Getter
    @Setter
    protected Ship ship;
    protected float energyCost;
    @Getter
    protected float reloadTimer, timeToReload;
    @Getter
    @Setter
    protected Vector2f localPosition;
    @Getter
    protected final int dataIndex;
    private final Polygon polygon;
    @Getter
    private final WeaponType type;
    @Getter
    private final BulletData bulletData;

    public WeaponSlot(GunData gunData, WeaponType type) {
        this.timeToReload = gunData.getReloadTimeInSeconds() * TimeUtils.UPDATES_PER_SECOND;
        this.energyCost = gunData.getEnergyCost();
        this.scale.set(gunData.getSizeX(), gunData.getSizeY());
        this.dataIndex = gunData.getDataIndex();
        this.type = type;
        this.polygon = gunData.getPolygon();
        this.bulletData = BulletRegistry.INSTANCE.get(gunData.getBulletData());
    }

    public WeaponSlot(GunData gunData) {
        this(gunData, WeaponType.GUN);
    }

    public void init(int id, Ship ship) {
        this.id = id;
        this.localPosition = ship.getWeaponSlotPosition(id);
        this.world = ship.getWorld();
        this.ship = ship;
        createBody();
        updatePos();
    }

    public void createBody() {
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

    protected void shoot() {
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        createBullet();
        reloadTimer = timeToReload;
        ship.getReactor().consume(energyCost);
    }

    protected void createBullet() {
        Bullet bullet = new Bullet(world, world.getNextId(), position.x, position.y, ship, bulletData);
        bullet.init();
        world.addBullet(bullet);
    }

    @Override
    public void update() {
        updatePos();
        if (reloadTimer > 0) {
            reloadTimer -= 50.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (reloadTimer < 0) reloadTimer = 0;
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

    public GunData getData() {
        return GunRegistry.INSTANCE.get(dataIndex);
    }

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