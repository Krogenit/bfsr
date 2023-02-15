package net.bfsr.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;

public abstract class WeaponSlotCommon extends TextureObject {
    @Getter
    @Setter
    protected int id;
    protected World world;
    @Getter
    @Setter
    protected ShipCommon ship;
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

    protected WeaponSlotCommon(ShipCommon ship, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY) {
        this.ship = ship;
        this.world = ship.getWorld();
        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.scale.set(scaleX, scaleY);
        this.alphaReducer = alphaReducer;
    }

    public void init(int id, Vector2f addPosition, ShipCommon ship) {
        this.addPosition = addPosition;
        this.ship = ship;
        createBody();
        this.id = id;
        updatePos();
        lastRotation = rotation;
        lastPosition.set(position);
    }

    public abstract void createBody();
    protected abstract void spawnShootParticles();

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    protected abstract void shoot();

    public void clientShoot() {

    }

    protected abstract void createBullet();

    @Override
    public void update() {
        lastPosition.set(position);

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

    @Override
    public void clear() {
        Body shipBody = ship.getBody();
        List<BodyFixture> bodyFixtures = shipBody.getFixtures();
        for (int i = 0, bodyFixturesSize = bodyFixtures.size(); i < bodyFixturesSize; i++) {
            BodyFixture bodyFixture = bodyFixtures.get(i);
            Object userData = bodyFixture.getUserData();
            if (userData == this) {
                shipBody.removeFixture(bodyFixture);
                ship.recalculateMass();
                break;
            }
        }
    }
}
