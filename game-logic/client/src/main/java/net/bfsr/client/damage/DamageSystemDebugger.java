package net.bfsr.client.damage;

import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.simplify.VWSimplifier;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DamageSystemDebugger {
    private final DamageSystem damageSystem = new DamageSystem();
    private DamageMaskTexture texture;

    public void init() {
        texture = new DamageMaskTexture(32, 32);
        texture.createEmpty();
    }

    public void testDifference() {
        Coordinate[] hull = {new Coordinate(3.4078212088324853, 0.4961343670098908),
                new Coordinate(2.966474771499634, -0.1420994997024536), new Coordinate(2.5226693153381348, 0.0901122093200684),
                new Coordinate(2.5028913021087646, 0.5906063318252563), new Coordinate(3, 0.949999988079071),
                new Coordinate(0.699999988079071, 2), new Coordinate(-1.7999999523162842, 2),
                new Coordinate(-3.3499999046325684, 1.149999976158142), new Coordinate(-3.700000047683716, -0.1000000014901161),
                new Coordinate(-3.3499999046325684, -1.399999976158142), new Coordinate(-1.7999999523162842, -2.200000047683716),
                new Coordinate(0.699999988079071, -2.200000047683716), new Coordinate(3, -1.2000000476837158),
                new Coordinate(3.619999885559082, -0.5), new Coordinate(3.4078212088324853, 0.4961343670098908)};

        Coordinate[] clip = {new Coordinate(-0.304789125919342, -1.3015059232711792),
                new Coordinate(-0.4895377159118652, -1.4834702014923096),
                new Coordinate(-0.5582969188690186, -1.7335007190704346),
                new Coordinate(-0.4925722479820251, -1.9843456745147705),
                new Coordinate(-0.3100420832633972, -2.1685352325439453),
                new Coordinate(-0.0598019361495972, -2.2365269660949707), new Coordinate(0.1908403635025024, -2.1700329780578613),
                new Coordinate(0.3744689226150513, -1.9869385957717896), new Coordinate(0.4416926801204681, -1.7364909648895264),
                new Coordinate(0.3744302093982697, -1.4860539436340332), new Coordinate(0.1907733082771301, -1.3029879331588745),
                new Coordinate(-0.0598793029785156, -1.2365329265594482),
                new Coordinate(-0.304789125919342, -1.3015059232711792)};

        Vector2f size = new Vector2f(13.913f, 13.913f);
        DamageMask damageMask = new DamageMask(32, 32);
        texture.fillEmpty();

        Polygon polygon = DamageSystem.GEOMETRY_FACTORY.createPolygon(DamageSystem.GEOMETRY_FACTORY.createLinearRing(hull));
        Polygon clipPolygon = DamageSystem.GEOMETRY_FACTORY.createPolygon(DamageSystem.GEOMETRY_FACTORY.createLinearRing(clip));

        Vector4f color = new Vector4f(0, 1, 0, 1f);
        renderPolygon(polygon, color);
        Vector4f clipColor = new Vector4f(1, 0, 0, 0.5f);
        renderPolygon(clipPolygon, clipColor);

        try {
            Geometry geometry = polygon.difference(clipPolygon);
            if (geometry instanceof Polygon polygon1) {
                org.locationtech.jts.geom.Geometry geometry1 = DamageSystem.optimizeAndReverse(polygon1);
                if (geometry1 instanceof Polygon polygon2) {
                    clipTextureOutside(polygon2, damageMask, size);
                    renderPolygon(polygon2, new Vector4f(1, 1, 0, 0.75f));
                }
            } else if (geometry instanceof MultiPolygon multiPolygon) {

            }

            int x = damageMask.getX();
            int y = damageMask.getY();
            int maxX = damageMask.getMaxX();
            int maxY = damageMask.getMaxY();
            int width = maxX - x + 1;
            int height = maxY - y + 1;
            if (width > 0 || height > 0) {
                ByteBuffer byteBuffer = Engine.renderer.createByteBuffer(width * height);
                byteBuffer.put(damageMask.getData()).flip();
                texture.upload(x, y, width, height, byteBuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clipTextureOutside(Polygon polygon, DamageMask mask, Vector2f scale) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = mask.getWidth() / scale.x;
        float localScaleY = mask.getHeight() / scale.y;

        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int size = coordinateSequence.size();
        float clippingOffset = -0.125f;
        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y += clippingOffset;
        }

        org.locationtech.jts.geom.Geometry geometry = BufferOp.bufferOp(polygon, DamageSystem.BUFFER_DISTANCE,
                DamageSystem.BUFFER_PARAMETERS);
        Geometry simplify = VWSimplifier.simplify(geometry, 0.1f);
        Coordinate[] coordinates = simplify.getCoordinates();

        AbstractDebugRenderer debugRenderer = Engine.renderer.debugRenderer;
        debugRenderer.addCommand(coordinates.length - 1);
        Vector4f color = new Vector4f(1, 0, 0, 1);
        for (int i = 0; i < coordinates.length - 1; i++) {
            Coordinate coordinate = coordinates[i];
            debugRenderer.addVertex((float) coordinate.x, (float) coordinate.y, color);
        }

        List<Coordinate> res = new ArrayList<>(coordinates.length - 1);
        for (int i = 0, length = coordinates.length - 1; i < length; i++) {
            Coordinate coordinate = coordinates[i];
            res.add(new Coordinate((coordinate.x + sizeX) * localScaleX, (coordinate.y + sizeY) * localScaleY));
        }
        DamageSystem.fillTextureOutsidePolygon(res, res.size(), mask, (byte) 0);

        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y -= clippingOffset;
        }
    }

    private void renderPolygon(Polygon polygon, Vector4f color) {
        AbstractDebugRenderer debugRenderer = Engine.renderer.debugRenderer;
        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int count = coordinateSequence.size() - 1;
        debugRenderer.addCommand(count);
        for (int i = 0; i < count; i++) {
            Coordinate coordinate = coordinateSequence.getCoordinate(i);
            debugRenderer.addVertex((float) coordinate.x, (float) coordinate.y, color);
        }
    }

    /**
     * Converts string to coordinates array
     *
     * @param args
     */
    public static void main(String[] args) {
        String input = "((3.4078212088324853 0.4961343670098908, 2.966474771499634 -0.1420994997024536, 2.5226693153381348 0.0901122093200684, 2.5028913021087646 0.5906063318252563, 3 0.949999988079071, 0.699999988079071 2, -1.7999999523162842 2, -3.3499999046325684 1.149999976158142, -3.700000047683716 -0.1000000014901161, -3.3499999046325684 -1.399999976158142, -1.7999999523162842 -2.200000047683716, 0.699999988079071 -2.200000047683716, 3 -1.2000000476837158, 3.619999885559082 -0.5, 3.4078212088324853 0.4961343670098908))";
        String input1 = "((-0.304789125919342 -1.3015059232711792, -0.4895377159118652 -1.4834702014923096, -0.5582969188690186 -1.7335007190704346, -0.4925722479820251 -1.9843456745147705, -0.3100420832633972 -2.1685352325439453, -0.0598019361495972 -2.2365269660949707, 0.1908403635025024 -2.1700329780578613, 0.3744689226150513 -1.9869385957717896, 0.4416926801204681 -1.7364909648895264, 0.3744302093982697 -1.4860539436340332, 0.1907733082771301 -1.3029879331588745, -0.0598793029785156 -1.2365329265594482, -0.304789125919342 -1.3015059232711792))";

        convertStringToCooridnates(input);
        convertStringToCooridnates(input1);
    }

    private static void convertStringToCooridnates(String s) {
        s = s.replace("(", "");
        s = s.replace(")", "");
        String[] coords = s.split(",");
        System.out.print("Coordinate[] coordinates = {");
        for (int i = 0; i < coords.length - 1; i++) {
            String coord = coords[i];
            if (coord.charAt(0) == ' ') coord = coord.substring(1);
            String[] values = coord.split(" ");
            System.out.print("new Coordinate(" + values[0] + ", " + values[1] + "), ");
        }

        String coord = coords[coords.length - 1];
        if (coord.charAt(0) == ' ') coord = coord.substring(1);
        String[] values = coord.split(" ");
        System.out.print("new Coordinate(" + values[0] + ", " + values[1] + ")};");
        System.out.println();
    }
}