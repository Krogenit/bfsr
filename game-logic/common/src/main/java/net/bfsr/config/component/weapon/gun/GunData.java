package net.bfsr.config.component.weapon.gun;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.entity.GameObjectConfigData;
import net.bfsr.engine.renderer.texture.TextureData;
import net.bfsr.engine.sound.SoundEffect;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.entity.bullet.BulletDamage;
import org.jbox2d.collision.shapes.Polygon;
import org.joml.Vector4f;

@Getter
public class GunData extends GameObjectConfigData {
    private final int reloadTimeInFrames;
    private final float energyCost;
    private final BulletDamage damage;
    private final Vector4f color;
    private final Polygon polygon;
    private final SoundEffect soundEffect;
    private final float bulletSpeed;
    private final int bulletLifeTimeInFrames;
    private final float bulletSizeX, bulletSizeY;
    private final TextureData bulletTextureData;
    private final Polygon bulletPolygon;
    private final float hp;

    public GunData(GunConfig config, String fileName, int id, int registryId) {
        super(config, fileName, id, registryId);
        this.reloadTimeInFrames = Engine.convertSecondsToFrames(config.reloadTimeInSeconds());
        this.energyCost = config.energyCost();
        this.damage = new BulletDamage(config.damage());
        this.color = convert(config.color());
        this.polygon = convertToPolygon(config.getVertices());
        this.soundEffect = convert(config.soundEffect());
        this.bulletSpeed = config.bulletSpeed();
        this.bulletLifeTimeInFrames = Engine.convertSecondsToFrames(config.bulletLifeTimeInSeconds());
        this.bulletSizeX = config.bulletSize().x();
        this.bulletSizeY = config.bulletSize().y();
        this.bulletTextureData = new TextureData(PathHelper.CLIENT_CONTENT.resolve(config.bulletTexture()));
        this.bulletPolygon = convertToPolygon(config.bulletVertices());
        this.hp = config.hp();
    }
}