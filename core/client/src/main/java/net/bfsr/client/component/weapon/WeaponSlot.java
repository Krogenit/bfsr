package net.bfsr.client.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.common.PacketWeaponShoot;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.world.WorldClient;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;
import java.util.Random;

public abstract class WeaponSlot extends TextureObject {
    @Getter
    @Setter
    protected int id;
    protected WorldClient world;
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
    private final SoundRegistry[] shootSounds;

    protected WeaponSlot(Ship ship, SoundRegistry[] shootSounds, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer,
                         float scaleX, float scaleY, TextureRegister texture) {
        super(TextureLoader.getTexture(texture), 0.0f, 0.0f, scaleX, scaleY);
        this.ship = ship;
        this.world = ship.getWorld();
        this.shootTimerMax = shootTimerMax;
        this.energyCost = energyCost;
        this.bulletSpeed = bulletSpeed;
        this.scale.set(scaleX, scaleY);
        this.alphaReducer = alphaReducer;
        this.shootSounds = shootSounds;
        this.color.set(1.0f, 1.0f, 1.0f, 1.0f);
        this.scale.set(scaleX, scaleY);
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

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    private void shoot() {
        Core.get().sendUDPPacket(new PacketWeaponShoot(ship.getId(), id));
    }

    public void clientShoot() {
        spawnShootParticles();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().consume(energyCost);
    }

    protected void playSound() {
        if (shootSounds != null) {
            int size = shootSounds.length;
            Random rand = world.getRand();
            SoundRegistry sound = shootSounds[rand.nextInt(size)];
            SoundSourceEffect source = new SoundSourceEffect(sound, position.x, position.y);
            Core.get().getSoundManager().play(source);
        }
    }

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

    public void render() {
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive() {

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
