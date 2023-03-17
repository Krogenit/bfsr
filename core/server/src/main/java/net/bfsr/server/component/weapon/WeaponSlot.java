package net.bfsr.server.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.GameObject;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;

public abstract class WeaponSlot extends GameObject {
    @Getter
    @Setter
    protected int id;
    protected WorldServer world;
    @Getter
    @Setter
    protected Ship ship;
    protected float energyCost;
    @Getter
    private final float bulletSpeed;
    @Getter
    protected float shootTimer, shootTimerMax;
    @Getter
    private final float alphaReducer;
    @Getter
    @Setter
    protected Vector2f addPosition;

    protected WeaponSlot(Ship ship, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY) {
        this.ship = ship;
        this.world = ship.getWorld();
        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.scale.set(scaleX, scaleY);
        this.alphaReducer = alphaReducer;
    }

    public void init(int id, Vector2f addPosition, Ship ship) {
        this.addPosition = addPosition;
        this.ship = ship;
        createBody();
        this.id = id;
        updatePos();
    }

    public abstract void createBody();

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    protected void shoot() {
        MainServer.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        createBullet();
        shootTimer = shootTimerMax;
        ship.getReactor().consume(energyCost);
    }

    protected abstract void createBullet();

    @Override
    public void update() {
        updatePos();
        if (shootTimer > 0) {
            shootTimer -= 50.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (shootTimer < 0) shootTimer = 0;
        }
    }

    public void updatePos() {
        Vector2f shipPos = ship.getPosition();
        float x = addPosition.x;
        float y = addPosition.y;
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * x - sin * y;
        float yPos = sin * x + cos * y;
        position.set(xPos + shipPos.x, yPos + shipPos.y);
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