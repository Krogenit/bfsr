package net.bfsr.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;
import java.util.Random;

public abstract class WeaponSlot extends TextureObject {
    @Getter
    @Setter
    protected int id;
    protected World world;
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
    private SoundRegistry[] shootSounds;

    protected WeaponSlot(Ship ship, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY) {
        this.ship = ship;
        world = ship.getWorld();
        color.set(1.0f, 1.0f, 1.0f, 1.0f);

        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.lastScale.set(scaleX, scaleY);
        this.scale.set(scaleX, scaleY);
        this.alphaReducer = alphaReducer;
    }

    protected WeaponSlot(Ship ship, SoundRegistry[] shootSounds, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY, TextureRegister texture) {
        this.shootSounds = shootSounds;
        this.ship = ship;
        world = ship.getWorld();
        color.set(1.0f, 1.0f, 1.0f, 1.0f);

        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.lastScale.set(scaleX, scaleY);
        this.scale.set(scaleX, scaleY);
        this.alphaReducer = alphaReducer;

        if (ship.getWorld().isRemote()) {
            this.texture = TextureLoader.getTexture(texture);
        }
    }

    public void init(int id, Vector2f addPosition, Ship ship) {
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

    public void shoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            if (world.isRemote()) {
                Core.getCore().sendPacket(new PacketWeaponShoot(ship.getId(), id));
            } else {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                createBullet();
                shootTimer = shootTimerMax;
                ship.getReactor().setEnergy(energy - energyCost);
            }
        }
    }

    public void clientShoot() {
        float energy = ship.getReactor().getEnergy();
        spawnShootParticles();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().setEnergy(energy - energyCost);
    }

    protected void playSound() {
        if (shootSounds != null) {
            int size = shootSounds.length;
            Random rand = world.getRand();
            SoundRegistry sound = shootSounds[rand.nextInt(size)];
            SoundSourceEffect source = new SoundSourceEffect(sound, position.x, position.y);
            Core.getCore().getSoundManager().play(source);
        }
    }

    protected abstract void createBullet();

    @Override
    public void update() {
        lastRotation = rotation;
        lastPosition.set(position);

        updatePos();
        if (shootTimer > 0) {
            shootTimer -= 50.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (shootTimer < 0) shootTimer = 0;
        }
    }

    public void updatePos() {
        rotation = ship.getRotation();
        Vector2f shipPos = ship.getPosition();
        float x = addPosition.x;
        float y = addPosition.y;
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * x - sin * y;
        float yPos = sin * x + cos * y;

        position.set(xPos + shipPos.x, yPos + shipPos.y);
    }

    public void renderAdditive(float interpolation) {

    }

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
