package net.bfsr.config;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.util.PathHelper;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Getter
public class ConfigData {
    private final String fileName;
    private final int id;
    private final int registryId;

    protected Polygon convertToPolygon(Vector2fConfigurable[] configurableVertices) {
        Vector2[] vertices = convert(configurableVertices);
        if (vertices.length > 0) {
            return new Polygon(vertices);
        }

        return null;
    }

    protected Polygon convert(PolygonConfigurable polygonConfigurable) {
        return new Polygon(convert(polygonConfigurable.getVertices()));
    }

    protected List<Polygon> convert(List<PolygonConfigurable> configPolygons) {
        ArrayList<Polygon> polygons = new ArrayList<>(configPolygons.size());
        for (int i = 0; i < configPolygons.size(); i++) {
            polygons.add(convert(configPolygons.get(i)));
        }
        return polygons;
    }

    protected Vector2[] convert(Vector2fConfigurable[] configurableVertices) {
        Vector2[] vertices = new Vector2[configurableVertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable configurableVertex = configurableVertices[i];
            vertices[i] = new Vector2(configurableVertex.x(), configurableVertex.y());
        }

        return vertices;
    }

    protected Vector2f convert(Vector2fConfigurable size) {
        return new Vector2f(size.x(), size.y());
    }

    protected Vector4f convert(ColorConfigurable effectsColor) {
        return new Vector4f(effectsColor.r(), effectsColor.g(), effectsColor.b(), effectsColor.a());
    }

    protected SoundData[] convert(ConfigurableSound[] configurableSounds) {
        SoundData[] sounds = new SoundData[configurableSounds.length];
        for (int i = 0; i < configurableSounds.length; i++) {
            ConfigurableSound configurableSound = configurableSounds[i];
            sounds[i] = new SoundData(PathHelper.convertPath(configurableSound.path()), configurableSound.volume());
        }

        return sounds;
    }

    protected <DEST_KEY, DEST_VALUE, SRC_KEY, SRC_VALUE> TMap<DEST_KEY, DEST_VALUE> convert(Map<SRC_KEY, SRC_VALUE> map,
                                                                                            Function<SRC_KEY, DEST_KEY> keyFunction,
                                                                                            Function<SRC_VALUE, DEST_VALUE> valueFunction) {
        TMap<DEST_KEY, DEST_VALUE> tMap = new THashMap<>();
        map.forEach((srcKey, srcValue) -> tMap.put(keyFunction.apply(srcKey), valueFunction.apply(srcValue)));
        return tMap;
    }

    protected org.locationtech.jts.geom.Polygon convertToJTSPolygon(Vector2fConfigurable[] vertices) {
        Coordinate[] coordinates = new Coordinate[vertices.length + 1];
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable vertex = vertices[i];
            coordinates[i] = new Coordinate(vertex.x(), vertex.y());
        }
        coordinates[vertices.length] = coordinates[0];
        return DamageSystem.GEOMETRY_FACTORY.createPolygon(DamageSystem.GEOMETRY_FACTORY.createLinearRing(coordinates));
    }
}