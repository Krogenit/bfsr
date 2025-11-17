package net.bfsr.config.entity.wreck;

import lombok.Getter;
import net.bfsr.engine.config.entity.GameObjectConfigData;
import org.jbox2d.collision.shapes.Polygon;

@Getter
public class WreckData extends GameObjectConfigData {
    private final Polygon polygon;

    public WreckData(WreckConfig wreckConfig, int id, int registryId) {
        super(wreckConfig, wreckConfig.getName(), id, registryId);
        this.polygon = new Polygon(convert(wreckConfig.getVertices()));
    }
}