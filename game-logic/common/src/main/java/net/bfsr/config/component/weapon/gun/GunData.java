package net.bfsr.config.component.weapon.gun;

import lombok.Getter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.config.SoundData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.entity.bullet.BulletDamage;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector4f;

import java.nio.file.Path;

@Getter
public class GunData extends GameObjectConfigData {
    private final int reloadTimeInTicks;
    private final float energyCost;
    private final BulletDamage damage;
    private final Vector4f color;
    private final Polygon polygon;
    private final SoundData[] sounds;
    private final float bulletSpeed;
    private final int bulletLifeTimeInTicks;
    private final float bulletSizeX, bulletSizeY;
    private final Path bulletTexture;
    private final Polygon bulletPolygon;
    private final float hp;

    public GunData(GunConfig config, String fileName, int id) {
        super(config, fileName, id);
        this.reloadTimeInTicks = Engine.convertToTicks(config.getReloadTimeInSeconds());
        this.energyCost = config.getEnergyCost();
        this.damage = new BulletDamage(config.getDamage());
        this.color = convert(config.getColor());
        this.polygon = convertToPolygon(config.getVertices());
        this.sounds = convert(config.getSounds());
        this.bulletSpeed = config.getBulletSpeed();
        this.bulletLifeTimeInTicks = Engine.convertToTicks(config.getBulletLifeTimeInSeconds());
        this.bulletSizeX = config.getBulletSize().x();
        this.bulletSizeY = config.getBulletSize().y();
        this.bulletTexture = PathHelper.CLIENT_CONTENT.resolve(config.getBulletTexture());
        this.bulletPolygon = convertToPolygon(config.getBulletVertices());
        this.hp = config.getHp();
    }
}