package net.bfsr.engine.config.entity;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.config.ConfigData;
import net.bfsr.engine.geometry.GeometryUtils;
import net.bfsr.engine.util.PathHelper;
import org.jbox2d.collision.shapes.Shape;
import org.locationtech.jts.geom.Polygon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Log4j2
public class GameObjectConfigData extends ConfigData {
    private final Path texture;
    private final float sizeX, sizeY;
    private final Polygon polygonJTS;
    private final List<Shape> shapeList;

    public GameObjectConfigData(GameObjectConfig config, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.sizeX = config.getSize().x();
        this.sizeY = config.getSize().y();
        this.texture = PathHelper.CLIENT_CONTENT.resolve(config.getTexture());
        this.polygonJTS = convertToJTSPolygon(config.getVertices());
        this.shapeList = new ArrayList<>();

        try {
            GeometryUtils.decompose(polygonJTS, shapeList::add);
        } catch (Exception e) {
            throw new RuntimeException("Can't decompose vertex data for config " + fileName, e);
        }
    }
}