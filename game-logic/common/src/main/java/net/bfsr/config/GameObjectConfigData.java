package net.bfsr.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.util.PathHelper;
import org.jbox2d.collision.shapes.Shape;
import org.locationtech.jts.geom.Polygon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Log4j2
public class GameObjectConfigData extends ConfigData {
    public static final float MIN_DISTANCE_BETWEEN_VERTICES_SQ = 0.3f;

    private final Path texture;
    private final float sizeX, sizeY;
    private final float minDistanceBetweenVerticesSq;
    private final Polygon polygonJTS;
    private final List<Shape> shapeList;

    public GameObjectConfigData(GameObjectConfig config, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.sizeX = config.getSize().x();
        this.sizeY = config.getSize().y();
        this.texture = PathHelper.CLIENT_CONTENT.resolve(config.getTexture());
        this.minDistanceBetweenVerticesSq = config.getMinDistanceBetweenVerticesSq();
        if (minDistanceBetweenVerticesSq < MIN_DISTANCE_BETWEEN_VERTICES_SQ) {
            throw new IllegalArgumentException("Min distance between vertices should be greater than " + MIN_DISTANCE_BETWEEN_VERTICES_SQ +
                    " in config " + fileName);
        }

        this.polygonJTS = convertToJTSPolygon(config.getVertices());
        this.shapeList = new ArrayList<>();

        try {
            DamageSystem.decompose(polygonJTS, shapeList::add);
        } catch (Exception e) {
            throw new RuntimeException("Can't decompose vertex data for config " + fileName, e);
        }
    }
}