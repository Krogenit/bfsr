package net.bfsr.client.component.weapon;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.common.PacketWeaponShoot;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.Sound;
import net.bfsr.client.sound.SoundLoader;
import net.bfsr.client.world.WorldClient;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.config.weapon.gun.GunData;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.PathHelper;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Random;

public class WeaponSlot extends TextureObject {
    @Getter
    @Setter
    protected int id;
    protected WorldClient world;
    @Getter
    @Setter
    protected Ship ship;
    protected float energyCost;
    @Getter
    protected float reloadTimer, timeToReload;
    @Getter
    @Setter
    protected Vector2f localPosition;
    private final Sound[] shootSounds;
    @Getter
    protected final Vector4f effectsColor;
    private final Polygon polygon;

    public WeaponSlot(Ship ship, GunData gunData) {
        super(TextureLoader.getTexture(gunData.getTexturePath()), 0.0f, 0.0f, gunData.getSizeX(), gunData.getSizeY());
        this.ship = ship;
        this.world = ship.getWorld();
        this.timeToReload = gunData.getReloadTimeInSeconds() * TimeUtils.UPDATES_PER_SECOND;
        this.energyCost = gunData.getEnergyCost();
        ConfigurableSound[] sounds = gunData.getSounds();
        this.shootSounds = new Sound[sounds.length];
        for (int i = 0; i < shootSounds.length; i++) {
            ConfigurableSound configurableSound = sounds[i];
            shootSounds[i] = new Sound(SoundLoader.getBuffer(PathHelper.convertPath(configurableSound.path())), configurableSound.volume());
        }

        this.effectsColor = new Vector4f(gunData.getColor());
        this.polygon = gunData.getPolygon();
    }

    public void init(int id) {
        this.id = id;
        this.localPosition = ship.getWeaponSlotPosition(id);
        createBody();
        updatePos();
        lastRotation = rotation;
        lastPosition.set(position);
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

    protected void spawnShootParticles() {
        Vector2f pos = RotationHelper.rotate(rotation, 1.0f, 0).add(getPosition());
        WeaponEffects.spawnWeaponShoot(pos, rotation, 8.0f, effectsColor.x, effectsColor.y, effectsColor.z, effectsColor.w);
    }

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (reloadTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    private void shoot() {
        Core.get().sendUDPPacket(new PacketWeaponShoot(ship.getId(), id));
    }

    public void clientShoot() {
        spawnShootParticles();
        playSound();
        reloadTimer = timeToReload;
        ship.getReactor().consume(energyCost);
    }

    protected void playSound() {
        if (shootSounds != null) {
            int size = shootSounds.length;
            Random rand = world.getRand();
            shootSounds[rand.nextInt(size)].play(position.x, position.y);
        }
    }

    @Override
    public void update() {
        lastPosition.set(position);

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

    public void render() {
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
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
                shipBody.updateMass();
                break;
            }
        }
    }
}