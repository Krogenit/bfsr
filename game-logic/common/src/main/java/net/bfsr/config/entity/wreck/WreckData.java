package net.bfsr.config.entity.wreck;

import lombok.Getter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.entity.wreck.WreckType;
import org.dyn4j.geometry.Polygon;

import java.nio.file.Path;

@Getter
public class WreckData extends GameObjectConfigData {
    private final WreckType type;
    private final Path fireTexture;
    private final Path sparkleTexture;
    private final Polygon polygon;

    WreckData(WreckConfig wreckConfig, int id) {
        super(wreckConfig, wreckConfig.getName(), id);
        this.type = wreckConfig.getType();
        this.fireTexture = PathHelper.convertPath(wreckConfig.getFireTexture());
        this.sparkleTexture =
                wreckConfig.getSparkleTexture() != null ? PathHelper.convertPath(wreckConfig.getSparkleTexture()) : null;
        this.polygon = new Polygon(convert(wreckConfig.getVertices()));
    }
}