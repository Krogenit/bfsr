package net.bfsr.config.component.weapon.gun;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.config.SoundData;
import net.bfsr.util.PathHelper;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector4f;

import java.nio.file.Path;

@Getter
public class GunData extends ConfigData {
    private final float sizeX, sizeY;
    private final Path texturePath;
    private final SoundData[] sounds;
    private final float reloadTimeInSeconds;
    private final float energyCost;
    private final Vector4f color;
    private final Polygon polygon;
    private final String bulletData;

    public GunData(GunConfig config, int dataIndex) {
        super(config.name(), dataIndex);
        this.sizeX = config.size().x();
        this.sizeY = config.size().y();
        this.texturePath = PathHelper.CLIENT_CONTENT.resolve(config.texture());
        this.color = convert(config.color());
        this.polygon = new Polygon(convertVertices(config.vertices()));
        this.sounds = convert(config.sounds());
        this.reloadTimeInSeconds = config.reloadTimeInSeconds();
        this.energyCost = config.energyCost();
        this.bulletData = config.bulletData();
    }
}