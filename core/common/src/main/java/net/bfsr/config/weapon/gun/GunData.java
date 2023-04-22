package net.bfsr.config.weapon.gun;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.util.PathHelper;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector4f;

import java.nio.file.Path;

@Getter
public class GunData extends ConfigData {
    private final float sizeX, sizeY;
    private final Path texturePath;
    private final ConfigurableSound[] sounds;
    private final float reloadTimeInSeconds;
    private final float energyCost;
    private final Vector4f color;
    private final Polygon polygon;
    private final String bulletData;

    public GunData(GunConfig config, int dataIndex) {
        super(dataIndex);
        this.sizeX = config.size().x();
        this.sizeY = config.size().y();
        this.texturePath = PathHelper.CLIENT_CONTENT.resolve(config.texture());
        this.color = new Vector4f(config.color().r(), config.color().g(), config.color().b(), config.color().a());
        this.polygon = new Polygon(convertVertices(config.vertices()));
        this.sounds = config.sounds();
        this.reloadTimeInSeconds = config.reloadTimeInSeconds();
        this.energyCost = config.energyCost();
        this.bulletData = config.bulletData();
    }
}