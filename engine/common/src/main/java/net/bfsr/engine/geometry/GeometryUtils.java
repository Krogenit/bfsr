package net.bfsr.engine.geometry;

import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import org.dyn4j.geometry.decompose.SweepLine;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vector2;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.function.Consumer;

public final class GeometryUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final SweepLine SWEEP_LINE = new SweepLine();

    public static void decompose(Polygon polygon, Consumer<org.jbox2d.collision.shapes.Polygon> polygonConsumer) {
        if (polygon.getExteriorRing().getNumPoints() > 4) {
            CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
            int count = coordinateSequence.size() - 1;
            Vector2[] vectors = new Vector2[count];
            for (int i = 0; i < count; i++) {
                Coordinate coordinate = coordinateSequence.getCoordinate(i);
                vectors[i] = new Vector2((float) coordinate.x, (float) coordinate.y);
            }

            List<Shape> convexes = SWEEP_LINE.decompose(vectors);
            for (int i = 0; i < convexes.size(); i++) {
                polygonConsumer.accept((org.jbox2d.collision.shapes.Polygon) convexes.get(i));
            }
        } else {
            CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
            Coordinate p0 = coordinateSequence.getCoordinate(0);
            Coordinate p1 = coordinateSequence.getCoordinate(1);
            Coordinate p2 = coordinateSequence.getCoordinate(2);
            polygonConsumer.accept(new org.jbox2d.collision.shapes.Polygon(new Vector2[]{new Vector2((float) p0.x, (float) p0.y),
                    new Vector2((float) p1.x, (float) p1.y), new Vector2((float) p2.x, (float) p2.y)}));
        }
    }

    public static Polygon createCirclePath(float x, float y, float sin, float cos, int count, float radius) {
        final float pin = MathUtils.TWO_PI / count;

        final float c = LUT.cos(pin);
        final float s = LUT.sin(pin);
        float t;

        float vertexX = radius;
        float vertexY = 0;

        Coordinate[] coordinates = new Coordinate[count + 1];
        for (int i = 0; i < count; i++) {
            float localPosX = vertexX + x;
            float localPosY = vertexY + y;
            coordinates[i] = new Coordinate(cos * localPosX - sin * localPosY, sin * localPosX + cos * localPosY);

            t = vertexX;
            vertexX = c * vertexX - s * vertexY;
            vertexY = s * t + c * vertexY;
        }

        coordinates[count] = coordinates[0];

        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    public static Polygon createCenteredRectanglePolygon(float width, float height, float x, float y, float sin,
                                                         float cos) {
        Coordinate[] coordinates = new Coordinate[5];
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        coordinates[0] = new Coordinate(-halfWidth, -halfHeight);
        coordinates[1] = new Coordinate(halfWidth, -halfHeight);
        coordinates[2] = new Coordinate(halfWidth, halfHeight);
        coordinates[3] = new Coordinate(-halfWidth, halfHeight);

        for (int i = 0; i < 4; i++) {
            Coordinate coordinate = coordinates[i];
            double localX = coordinate.x;
            double localY = coordinate.y;
            coordinate.setX(cos * localX - sin * localY + x);
            coordinate.setY(sin * localX + cos * localY + y);
        }

        coordinates[4] = coordinates[0];

        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    public static Polygon createCenteredRhombusPolygon(float width, float height, float x, float y, float sin,
                                                       float cos) {
        Coordinate[] coordinates = new Coordinate[5];
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        coordinates[0] = new Coordinate(-halfWidth, 0.0f);
        coordinates[1] = new Coordinate(0.0f, -halfHeight);
        coordinates[2] = new Coordinate(halfWidth, 0.0f);
        coordinates[3] = new Coordinate(0.0f, halfHeight);

        for (int i = 0; i < 4; i++) {
            Coordinate coordinate = coordinates[i];
            double localX = coordinate.x;
            double localY = coordinate.y;
            coordinate.setX(cos * localX - sin * localY + x);
            coordinate.setY(sin * localX + cos * localY + y);
        }

        coordinates[4] = coordinates[0];

        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    public static Polygon createPolygon(LinearRing linearRing) {
        return GEOMETRY_FACTORY.createPolygon(linearRing);
    }

    public static Geometry createPoint(Coordinate coordinate) {
        return GEOMETRY_FACTORY.createPoint(coordinate);
    }

    public static LinearRing createLinearRing(Coordinate[] coordinates) {
        return GEOMETRY_FACTORY.createLinearRing(coordinates);
    }

    public static Polygon createPolygon(LinearRing linearRing, LinearRing[] holes) {
        return GEOMETRY_FACTORY.createPolygon(linearRing, holes);
    }
}
