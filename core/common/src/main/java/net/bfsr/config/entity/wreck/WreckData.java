package net.bfsr.config.entity.wreck;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.util.PathHelper;
import org.dyn4j.geometry.Polygon;

import java.nio.file.Path;

@Getter
public class WreckData extends ConfigData {
    private final WreckType type;
    private final Path texture;
    private final Path fireTexture;
    private final Path sparkleTexture;
    private final Polygon polygon;

    public WreckData(WreckConfig wreckConfig, int dataIndex) {
        super(wreckConfig.name(), dataIndex);
        this.type = wreckConfig.type();
        this.texture = PathHelper.convertPath(wreckConfig.texturePath());
        this.fireTexture = PathHelper.convertPath(wreckConfig.fireTexturePath());
        this.sparkleTexture = wreckConfig.sparkleTexturePath() != null ? PathHelper.convertPath(wreckConfig.sparkleTexturePath()) : null;
        this.polygon = new Polygon(convertVertices(wreckConfig.vertices()));
    }
}