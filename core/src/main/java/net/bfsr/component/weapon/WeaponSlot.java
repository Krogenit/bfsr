package net.bfsr.component.weapon;

import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.component.EnumWeapon;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.MainServer;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Random;

public abstract class WeaponSlot extends TextureObject {

    protected int id;
    protected World world;
    protected Ship ship;
    protected float energyCost;
    private final float bulletSpeed;
    protected float shootTimer, shootTimerMax;
    private final float alphaReducer;
    protected Vector2f addPosition;
    private EnumWeapon weaponType;
    private SoundRegistry[] shootSounds;

    public WeaponSlot(Ship ship, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, Vector2f scale) {
        this.ship = ship;
        this.world = ship.getWorld();
        this.color = new Vector4f(1, 1, 1, 1);

        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.scale = scale;
        this.alphaReducer = alphaReducer;
    }

    public WeaponSlot(Ship ship, SoundRegistry[] shootSounds, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, Vector2f scale, TextureRegister texture) {
        this.shootSounds = shootSounds;
        this.ship = ship;
        this.world = ship.getWorld();
        this.color = new Vector4f(1, 1, 1, 1);

        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.scale = scale;
        this.alphaReducer = alphaReducer;

        if (ship.getWorld().isRemote()) {
            this.texture = TextureLoader.getTexture(texture);
        }
    }

    public void createBody() {

    }

    protected abstract void spawnShootParticles();

    public void shoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            if (world.isRemote()) {
                Core.getCore().sendPacket(new PacketWeaponShoot(ship.getId(), id));
            } else {
                MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
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
            SoundSourceEffect source = new SoundSourceEffect(sound, position);
            Core.getCore().getSoundManager().play(source);
        }
    }

    protected abstract void createBullet();

    @Override
    public void update(double delta) {
        updatePos();
        if (shootTimer > 0) {
            shootTimer -= 50f * delta;
            if (shootTimer < 0) shootTimer = 0;
        }
    }

    public void updatePos() {
        this.rotate = ship.getRotation();
        Vector2f shipPos = ship.getPosition();
        float x = addPosition.x;
        float y = addPosition.y;
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * x - sin * y;
        float yPos = sin * x + cos * y;

        this.position = new Vector2f(xPos + shipPos.x, yPos + shipPos.y);
    }

    @Override
    public void render(BaseShader shader) {
        super.render(shader);
    }

    public Vector2f getAddPosition() {
        return addPosition;
    }

    public void setAddPosition(Vector2f addPosition) {
        this.addPosition = addPosition;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void clear() {
        Body shipBody = ship.getBody();
        List<BodyFixture> bodyFixtures = shipBody.getFixtures();
        for (BodyFixture bodyFixture : bodyFixtures) {
            Object userData = bodyFixture.getUserData();
            if (userData == this) {
                shipBody.removeFixture(bodyFixture);
                ship.recalculateMass();
                break;
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getShootTimer() {
        return shootTimer;
    }

    public float getShootTimerMax() {
        return shootTimerMax;
    }

    public float getBulletSpeed() {
        return bulletSpeed;
    }

    public float getEnergyCost() {
        return energyCost;
    }

    public float getAlphaReducer() {
        return alphaReducer;
    }

    public Ship getShip() {
        return ship;
    }
}
