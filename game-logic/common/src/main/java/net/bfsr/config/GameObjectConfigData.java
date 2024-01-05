package net.bfsr.config;

import clipper2.core.PathD;
import clipper2.core.PointD;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.util.PathHelper;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.decompose.SweepLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Log4j2
public class GameObjectConfigData extends ConfigData {
    private static final SweepLine SWEEP_LINE = new SweepLine();

    private final Path texture;
    private final float sizeX, sizeY;
    private final PathD contour;
    private final List<Convex> convexList;

    public GameObjectConfigData(GameObjectConfig config, String fileName, int id) {
        super(fileName, id);
        this.sizeX = config.getSize().x();
        this.sizeY = config.getSize().y();
        this.texture = PathHelper.CLIENT_CONTENT.resolve(config.getTexture());
        Vector2fConfigurable[] vertices = config.getVertices();
        this.contour = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable vertex = vertices[i];
            this.contour.add(new PointD(vertex.x(), vertex.y()));
        }

        List<Convex> convexes;

        if (vertices.length > 3) {
            try {
                convexes = SWEEP_LINE.decompose(convert(vertices));
            } catch (Exception e) {
                throw new RuntimeException("Can't decompose vertex data for config " + fileName, e);
            }
        } else {
            convexes = new ArrayList<>(1);
            convexes.add(convertToPolygon(vertices));
        }

        this.convexList = convexes;
    }
}