package net.bfsr.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.util.PathHelper;
import org.dyn4j.geometry.Convex;
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
    private final List<Convex> convexList;

    public GameObjectConfigData(GameObjectConfig config, String fileName, int id) {
        super(fileName, id);
        this.sizeX = config.getSize().x();
        this.sizeY = config.getSize().y();
        this.texture = PathHelper.CLIENT_CONTENT.resolve(config.getTexture());
        this.polygonJTS = convertToJTSPolygon(config.getVertices());
        this.convexList = new ArrayList<>();

        try {
            DamageSystem.decompose(polygonJTS, convexList::add);
        } catch (Exception e) {
            throw new RuntimeException("Can't decompose vertex data for config " + fileName, e);
        }
    }
}