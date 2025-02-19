package net.bfsr.client.damage;

import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.simplify.VWSimplifier;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DamageSystemDebugger {
    private final AbstractRenderer renderer = Engine.getRenderer();
    private final float minDistanceBetweenVertices = 0.3f;
    private final float bufferDistance = 0.3f;

    private DamageMaskTexture texture;

    public void init() {
        texture = new DamageMaskTexture(32, 32);
        texture.createEmpty();
    }

    public void testDifference() {
        Coordinate[] hull = {new Coordinate(-0.02753130793489883, 0.9344961650314437),
                new Coordinate(0.18288369476795197, 0.7380020618438721), new Coordinate(0.4642007052898407, -0.5030548572540283),
                new Coordinate(0.02367682382464409, -0.7414337992668152), new Coordinate(-0.40248996019363403, -0.47824621200561523),
                new Coordinate(-0.4137935844988728, 0.041737761720493566), new Coordinate(-1.0426856319976003, 0.9149183293545877),
                new Coordinate(-1.262723684310913, 0.36883023381233215), new Coordinate(-1.7636122703552246, 0.36972111463546753),
                new Coordinate(-1.839916549295036, 1.0982215238680728), new Coordinate(-2.187290668487549, 0.7968834638595581),
                new Coordinate(-2.749799966812134, 0.9165999889373779), new Coordinate(-3.345590114593506, -0.05041299760341644),
                new Coordinate(-2.786929612423215, -1.0653414314225045), new Coordinate(-2.749799966812134, -1.5123900175094604),
                new Coordinate(-0.27498000860214233, -0.8249399662017822), new Coordinate(-0.35456115274657735, -1.7190307583185893),
                new Coordinate(-0.1698736539974568, -2.325520489748136), new Coordinate(0.5510676317705674, -2.7956299781799316),
                new Coordinate(1.198148506968871, -2.346779820615142), new Coordinate(1.2190780639648438, -0.9165999889373779),
                new Coordinate(3.296734597488099, -0.9003056597895891), new Coordinate(3.6729098765337707, -0.36255628796866124),
                new Coordinate(3.7580599784851074, 0.7791100144386292), new Coordinate(1.2190780639648438, 0.7791100144386292),
                new Coordinate(1.1915799379348755, 2.658139944076538), new Coordinate(0.6796393394470215, 2.237511157989502),
                new Coordinate(0.1792927384376526, 2.214205503463745), new Coordinate(-0.0853701687414741, 2.658139944076538),
                new Coordinate(-0.45719589930760485, 2.380364523929982), new Coordinate(-0.09276704490184784, 1.9147783517837524),
                new Coordinate(-0.29053744399954856, 1.1801020114540648), new Coordinate(-0.02753130793489883, 0.9344961650314437)};

        Coordinate[] clip = {new Coordinate(-0.24692530930042267, 0.9995884299278259),
                new Coordinate(-0.49993982911109924, 0.9427840709686279), new Coordinate(-0.6904749274253845, 0.7668879628181458),
                new Coordinate(-0.7672817707061768, 0.5192111730575562), new Coordinate(-0.7097014784812927, 0.26637211441993713),
                new Coordinate(-0.5332216024398804, 0.07637755572795868), new Coordinate(-0.2853103578090668, 3.3087562769651413E-4),
                new Coordinate(-0.03264913335442543, 0.05868659168481827), new Coordinate(0.15680308640003204, 0.2357485443353653),
                new Coordinate(0.23208880424499512, 0.48389193415641785), new Coordinate(0.17295826971530914, 0.7363729476928711),
                new Coordinate(-0.004684064537286758, 0.9252811074256897), new Coordinate(-0.24692530930042267, 0.9995884299278259)};

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
                org.locationtech.jts.geom.Geometry geometry1 = DamageSystem.optimizeAndReverse(polygon1, minDistanceBetweenVertices);
                if (geometry1 instanceof Polygon polygon2) {
                    clipTextureOutside(polygon2, damageMask, size);

                    DamageSystem.decompose(polygon2, polygon3 -> {});

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
                ByteBuffer byteBuffer = renderer.createByteBuffer(width * height);
                byteBuffer.put(damageMask.getData()).flip();
                texture.upload(x, y, width, height, byteBuffer);
                renderer.memFree(byteBuffer);
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

        org.locationtech.jts.geom.Geometry geometry = BufferOp.bufferOp(polygon, bufferDistance, DamageSystem.BUFFER_PARAMETERS);
        Geometry simplify = VWSimplifier.simplify(geometry, 0.1f);
        Coordinate[] coordinates = simplify.getCoordinates();

        AbstractDebugRenderer debugRenderer = renderer.getDebugRenderer();
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
        DamageSystem.fillMaskOutsidePolygon(res, res.size(), mask, (byte) 0);

        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y -= clippingOffset;
        }
    }

    private void renderPolygon(Polygon polygon, Vector4f color) {
        AbstractDebugRenderer debugRenderer = renderer.getDebugRenderer();
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
        String input = "-0.02753130793489883 0.9344961650314437, 0.18288369476795197 0.7380020618438721, 0.4642007052898407 -0.5030548572540283, 0.02367682382464409 -0.7414337992668152, -0.40248996019363403 -0.47824621200561523, -0.4137935844988728 0.041737761720493566, -1.0426856319976003 0.9149183293545877, -1.262723684310913 0.36883023381233215, -1.7636122703552246 0.36972111463546753, -1.839916549295036 1.0982215238680728, -2.187290668487549 0.7968834638595581, -2.749799966812134 0.9165999889373779, -3.345590114593506 -0.05041299760341644, -2.786929612423215 -1.0653414314225045, -2.749799966812134 -1.5123900175094604, -0.27498000860214233 -0.8249399662017822, -0.35456115274657735 -1.7190307583185893, -0.1698736539974568 -2.325520489748136, 0.5510676317705674 -2.7956299781799316, 1.198148506968871 -2.346779820615142, 1.2190780639648438 -0.9165999889373779, 3.296734597488099 -0.9003056597895891, 3.6729098765337707 -0.36255628796866124, 3.7580599784851074 0.7791100144386292, 1.2190780639648438 0.7791100144386292, 1.1915799379348755 2.658139944076538, 0.6796393394470215 2.237511157989502, 0.1792927384376526 2.214205503463745, -0.0853701687414741 2.658139944076538, -0.45719589930760485 2.380364523929982, -0.09276704490184784 1.9147783517837524, -0.29053744399954856 1.1801020114540648, -0.02753130793489883 0.9344961650314437";

        String input1 = "-0.24692530930042267 0.9995884299278259, -0.49993982911109924 0.9427840709686279, -0.6904749274253845 0.7668879628181458, -0.7672817707061768 0.5192111730575562, -0.7097014784812927 0.26637211441993713, -0.5332216024398804 0.07637755572795868, -0.2853103578090668 3.3087562769651413E-4, -0.03264913335442543 0.05868659168481827, 0.15680308640003204 0.2357485443353653, 0.23208880424499512 0.48389193415641785, 0.17295826971530914 0.7363729476928711, -0.004684064537286758 0.9252811074256897, -0.24692530930042267 0.9995884299278259";

        convertStringToCooridnates(input);
        convertStringToCooridnates(input1);
    }

    private static void convertStringToCooridnates(String s) {
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