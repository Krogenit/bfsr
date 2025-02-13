package net.bfsr.damage;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.math.RotationHelper;
import net.bfsr.world.World;
import org.dyn4j.geometry.decompose.SweepLine;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.simplify.VWSimplifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
public final class DamageSystem {
    private static final float BUFFER_Y_OFFSET = -0.16f;
    public static final double BUFFER_DISTANCE = 0.3f;
    private static final double MIN_POLYGON_AREA = 0.3;
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final SweepLine SWEEP_LINE = new SweepLine();
    public static final BufferParameters BUFFER_PARAMETERS = new BufferParameters(1, BufferParameters.CAP_SQUARE,
            BufferParameters.JOIN_MITRE, 0.1);

    private final Vector2f rotatedLocalCenter = new Vector2f();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public void damage(DamageableRigidBody damageable, float contactX, float contactY, org.locationtech.jts.geom.Polygon clip,
                       float textureClipRadius, float x, float y, float sin, float cos, Runnable onDamageSuccessRunnable) {
        if (damageable.isDead()) {
            return;
        }

        DamageMask mask = damageable.getDamageMask();
        mask.reset();
        damageable.removeHullFixtures();

        clipTexture(contactX, contactY, -sin, cos, damageable, textureClipRadius, mask, damageable.getLocalOffsetX(),
                damageable.getLocalOffsetY());

        org.locationtech.jts.geom.Polygon polygon = damageable.getPolygon();

        org.locationtech.jts.geom.Geometry difference = polygon.difference(clip);

        if (difference instanceof MultiPolygon multiPolygon) {
            processMultiPolygon(damageable, multiPolygon, x, y, sin, cos, true);
        } else if (difference instanceof org.locationtech.jts.geom.Polygon polygon1) {
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_POLYGON_AREA) {
                org.locationtech.jts.geom.Geometry geometry = optimizeAndReverse(polygon1,
                        damageable.getConfigData().getMinDistanceBetweenVerticesSq());
                if (geometry instanceof org.locationtech.jts.geom.Polygon polygon2) {
                    clipTextureOutside(polygon2, mask, damageable.getSizeX(), damageable.getSizeY(), damageable.getLocalOffsetX(),
                            damageable.getLocalOffsetY());
                    damageable.setPolygon(polygon2);
                    decompose(polygon2, polygon3 -> damageable.addHullFixture(damageable.setupFixture(new Fixture(polygon3))));

                    List<ConnectedObject<?>> connectedObjects = damageable.getConnectedObjects();
                    for (int i = 0; i < connectedObjects.size(); i++) {
                        ConnectedObject<?> connectedObject = connectedObjects.get(i);
                        if (!connectedObject.isInside(polygon2)) {
                            damageable.removeConnectedObject(connectedObject);
                            connectedObject.spawn();
                        }
                    }

                    damageable.onContourReconstructed(polygon2);
                } else if (geometry instanceof MultiPolygon multiPolygon) {
                    processMultiPolygon(damageable, multiPolygon, x, y, sin, cos, false);
                }
            } else {
                damageable.setDead();
            }
        } else {
            damageable.setDead();
        }

        if (!damageable.isDead() && mask.dirty()) {
            onDamageSuccessRunnable.run();
        }
    }

    private void processMultiPolygon(DamageableRigidBody damageable, MultiPolygon multiPolygon, double x, double y, double sin, double cos,
                                     boolean optimize) {
        int polygonsCount = multiPolygon.getNumGeometries();
        double minDistance = Double.MAX_VALUE;
        org.locationtech.jts.geom.Polygon newHull = null;
        List<org.locationtech.jts.geom.Polygon> removedPaths = new ArrayList<>(polygonsCount - 1);
        for (int i = 0; i < polygonsCount; i++) {
            org.locationtech.jts.geom.Polygon polygon1 = (org.locationtech.jts.geom.Polygon) multiPolygon.getGeometryN(i);
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_POLYGON_AREA) {
                if (optimize) {
                    org.locationtech.jts.geom.Geometry geometry = optimizeAndReverse(polygon1,
                            damageable.getConfigData().getMinDistanceBetweenVerticesSq());

                    if (geometry instanceof org.locationtech.jts.geom.Polygon polygon2) {
                        Coordinate center = polygon2.getCentroid().getCoordinate();
                        double distance = center.x * center.x + center.y * center.y;
                        if (distance < minDistance) {
                            if (newHull != null) {
                                removedPaths.add(newHull);
                            }
                            newHull = polygon2;
                            minDistance = distance;
                        } else {
                            removedPaths.add(polygon2);
                        }
                    } else if (geometry instanceof MultiPolygon multiPolygon1) {
                        int polygonsCount1 = multiPolygon1.getNumGeometries();
                        for (int j = 0; j < polygonsCount1; j++) {
                            org.locationtech.jts.geom.Polygon polygon2 = (org.locationtech.jts.geom.Polygon) multiPolygon1.getGeometryN(j);
                            area = Area.ofRing(polygon2.getExteriorRing().getCoordinateSequence());
                            if (area > MIN_POLYGON_AREA) {
                                Coordinate center = polygon2.getCentroid().getCoordinate();
                                double distance = center.x * center.x + center.y * center.y;
                                if (distance < minDistance) {
                                    if (newHull != null) {
                                        removedPaths.add(newHull);
                                    }
                                    newHull = polygon2;
                                    minDistance = distance;
                                } else {
                                    removedPaths.add(polygon2);
                                }
                            }
                        }
                    }
                } else {
                    Coordinate center = polygon1.getCentroid().getCoordinate();
                    double distance = center.x * center.x + center.y * center.y;
                    if (distance < minDistance) {
                        if (newHull != null) {
                            removedPaths.add(newHull);
                        }
                        newHull = polygon1;
                        minDistance = distance;
                    } else {
                        removedPaths.add(polygon1);
                    }
                }
            }
        }

        List<ConnectedObject<?>> removedConnectedObjects = new ArrayList<>();
        if (newHull != null) {
            damageable.setPolygon(newHull);
            decompose(newHull, polygon1 -> damageable.addHullFixture(damageable
                    .setupFixture(new Fixture(polygon1))));

            List<ConnectedObject<?>> connectedObjects = damageable.getConnectedObjects();
            for (int i = 0; i < connectedObjects.size(); i++) {
                ConnectedObject<?> connectedObject = connectedObjects.get(i);
                if (!connectedObject.isInside(newHull)) {
                    damageable.removeConnectedObject(connectedObject);
                    removedConnectedObjects.add(connectedObject);
                }
            }

            damageable.onContourReconstructed(newHull);
        } else {
            damageable.setDead();
        }

        float sizeX = damageable.getSizeX();
        float sizeY = damageable.getSizeY();
        World world = damageable.getWorld();
        DamageMask mask = damageable.getDamageMask();

        for (int i = 0; i < removedPaths.size(); i++) {
            org.locationtech.jts.geom.Polygon removedPath = removedPaths.get(i);
            DamageMask damageMask = createInvertedDamageMask(removedPath, mask, sizeX, sizeY);
            Coordinate localCenter = removedPath.getCentroid().getCoordinate();
            ShipWreck wreck = createWreck(world, x, y, sin, cos, sizeX, sizeY, removedPath,
                    damageMask, (ShipData) damageable.getConfigData(), localCenter);
            wreck.setLinearVelocity(damageable.getLinearVelocity());
            wreck.setAngularVelocity(damageable.getAngularVelocity());

            for (int j = 0; j < removedConnectedObjects.size(); j++) {
                ConnectedObject<?> connectedObject = removedConnectedObjects.get(j);
                if (connectedObject.isInside(removedPath, (float) -localCenter.x, (float) -localCenter.y)) {
                    wreck.addConnectedObject(connectedObject);
                    connectedObject.addPositionOffset((float) -localCenter.x, (float) -localCenter.y);
                } else {
                    connectedObject.spawn();
                }
            }

            world.add(wreck);
        }

        if (!damageable.isDead()) {
            clipTextureOutside(newHull, mask, sizeX, sizeY, damageable.getLocalOffsetX(), damageable.getLocalOffsetY());
        }
    }

    public static org.locationtech.jts.geom.Geometry optimizeAndReverse(org.locationtech.jts.geom.Polygon polygon,
                                                                        float minDistanceBetweenVerticesSq) {
        int numInteriorRing = polygon.getNumInteriorRing();
        if (numInteriorRing > 0) {
            polygon = GEOMETRY_FACTORY.createPolygon(polygon.getExteriorRing());
        }

        return VWSimplifier.simplify(polygon, minDistanceBetweenVerticesSq).reverse();
    }

    public static void fillTextureOutsidePolygon(List<Coordinate> coordinates, int count, DamageMask damageMask, byte value) {
        int[] nodeX = new int[count];
        int nodes, i, j, swap, pixelX;
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        byte[] data = damageMask.getData();

        for (int pixelY = 0; pixelY < damageMask.getHeight(); pixelY++) {
            nodes = 0;
            j = count - 1;
            for (i = 0; i < count; i++) {
                Coordinate pointD = coordinates.get(i);
                Coordinate pointD1 = coordinates.get(j);
                if (pointD.y < pixelY && pointD1.y >= pixelY || pointD1.y < pixelY && pointD.y >= pixelY) {
                    nodeX[nodes++] = (int) (pointD.x + (pixelY - pointD.y) / (pointD1.y - pointD.y) * (pointD1.x - pointD.x));
                }
                j = i;
            }

            boolean onePixelPut = false;
            if (nodes > 0) {
                i = 0;
                while (i < nodes - 1) {
                    if (nodeX[i] > nodeX[i + 1]) {
                        swap = nodeX[i];
                        nodeX[i] = nodeX[i + 1];
                        nodeX[i + 1] = swap;
                        if (i > 0) i--;
                    } else {
                        i++;
                    }
                }

                int startX = 0;
                int endX = nodeX[0];
                for (i = 1; i < nodes; i += 2) {
                    for (pixelX = startX; pixelX < endX; pixelX++) {
                        int index = pixelY * damageMask.getHeight() + pixelX;
                        byte currValue = data[index];
                        if (currValue != value) {
                            data[index] = value;
                            onePixelPut = true;

                            if (pixelX < x) x = pixelX;
                            if (pixelX > maxX) maxX = pixelX;
                        }
                    }
                    startX = nodeX[i] + 1;
                    endX = i + 1 == nodes ? damageMask.getWidth() : nodeX[i + 1];
                }

                for (pixelX = startX; pixelX < damageMask.getWidth(); pixelX++) {
                    int index = pixelY * damageMask.getHeight() + pixelX;
                    byte currValue = data[index];
                    if (currValue != value) {
                        data[index] = value;
                        onePixelPut = true;

                        if (pixelX < x) x = pixelX;
                        if (pixelX > maxX) maxX = pixelX;
                    }
                }
            } else {
                for (pixelX = 0; pixelX < damageMask.getWidth(); pixelX++) {
                    int index = pixelY * damageMask.getHeight() + pixelX;
                    byte currValue = data[index];
                    if (currValue != value) {
                        data[index] = value;
                        onePixelPut = true;

                        if (pixelX < x) x = pixelX;
                        if (pixelX > maxX) maxX = pixelX;
                    }
                }
            }

            if (onePixelPut) {
                if (pixelY < y) y = pixelY;
                if (pixelY > maxY) maxY = pixelY;
            }
        }

        damageMask.setX(Math.min(damageMask.getX(), x));
        damageMask.setY(Math.min(damageMask.getY(), y));
        damageMask.setMaxX(Math.max(damageMask.getMaxX(), maxX));
        damageMask.setMaxY(Math.max(damageMask.getMaxY(), maxY));
    }

    private static List<Coordinate> clipTextureOutside(org.locationtech.jts.geom.Polygon polygon, DamageMask mask, float sizeX,
                                                       float sizeY, float offsetX, float offsetY) {
        float halfSizeX = sizeX / 2.0f;
        float halfSizeY = sizeY / 2.0f;
        float localScaleX = mask.getWidth() / sizeX;
        float localScaleY = mask.getHeight() / sizeY;

        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int size = coordinateSequence.size();
        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y += BUFFER_Y_OFFSET;
        }

        org.locationtech.jts.geom.Polygon polygon1 = (org.locationtech.jts.geom.Polygon) BufferOp.bufferOp(polygon, BUFFER_DISTANCE,
                BUFFER_PARAMETERS);

        CoordinateSequence coordinateSequence1 = polygon1.getExteriorRing().getCoordinateSequence();
        List<Coordinate> res = new ArrayList<>(coordinateSequence1.size() - 1);
        for (int i = 0, length = coordinateSequence1.size() - 1; i < length; i++) {
            Coordinate coordinate = coordinateSequence1.getCoordinate(i);
            res.add(new Coordinate((coordinate.x + halfSizeX + offsetX) * localScaleX, (coordinate.y + halfSizeY + offsetY) * localScaleY));
        }
        fillTextureOutsidePolygon(res, res.size(), mask, (byte) 0);

        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y -= BUFFER_Y_OFFSET;
        }

        return res;
    }

    private void clipTexture(float x, float y, float sin, float cos, DamageableRigidBody damageable, float clipRadius,
                             DamageMask mask, float localOffsetX, float localOffsetY) {
        float sizeX = damageable.getSizeX();
        float sizeY = damageable.getSizeY();
        float halfSizeX = sizeX / 2.0f;
        float halfSizeY = sizeY / 2.0f;
        int width = mask.getWidth();
        int height = mask.getHeight();
        int radius = (int) Math.ceil(clipRadius * (width / sizeX) / 2.0f);

        float localPosX = x - damageable.getX();
        float localPosY = y - damageable.getY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = sin * localPosX + cos * localPosY;
        int localX = (int) ((rotatedX + halfSizeX + localOffsetX) * (width / sizeX));
        int localY = (int) ((rotatedY + halfSizeY + localOffsetY) * (height / sizeY));
        int startX = Math.max(localX - radius, 0);
        int startY = Math.max(localY - radius, 0);
        int maxX = Math.min(localX + radius, width);
        int maxY = Math.min(localY + radius, height);

        if (maxX - startX > 0 && maxY - startY > 0) {
            int radiusSq = radius * radius;
            byte value = 0;
            byte[] data = mask.getData();

            for (int j = startY; j < maxY; j++) {
                for (int i = startX; i < maxX; i++) {
                    int dx = i - localX;
                    int dy = j - localY;
                    float square = (dx * dx + dy * dy) * (random.nextFloat(0.5f) + 0.5f);
                    if (square < radiusSq) {
                        int index = j * height + i;
                        float holeThreshold = radiusSq / 4.0f * random.nextFloat();
                        if (square <= holeThreshold) {
                            data[index] = value;
                        } else {
                            data[index] = (byte) Math.min((int) (((square - holeThreshold) / radiusSq) * 255),
                                    Byte.toUnsignedInt(data[index]));
                        }
                    }
                }
            }

            mask.setX(startX);
            mask.setY(startY);
            mask.setMaxX(maxX - 1);
            mask.setMaxY(maxY - 1);
        }
    }

    public static void decompose(org.locationtech.jts.geom.Polygon polygon, Consumer<Polygon> polygonConsumer) {
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
                polygonConsumer.accept((Polygon) convexes.get(i));
            }
        } else {
            CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
            Coordinate p0 = coordinateSequence.getCoordinate(0);
            Coordinate p1 = coordinateSequence.getCoordinate(1);
            Coordinate p2 = coordinateSequence.getCoordinate(2);
            polygonConsumer.accept(new Polygon(new Vector2[]{new Vector2((float) p0.x, (float) p0.y),
                    new Vector2((float) p1.x, (float) p1.y), new Vector2((float) p2.x, (float) p2.y)}));
        }
    }

    private DamageMask createInvertedDamageMask(org.locationtech.jts.geom.Polygon polygon, DamageMask damageMask, float sizeX,
                                                float sizeY) {
        DamageMask damagedTexture = new DamageMask(damageMask);
        List<Coordinate> path = clipTextureOutside(polygon, damagedTexture, sizeX, sizeY, 0, 0);
        for (int i = 0; i < path.size(); i++) {
            Coordinate point = path.get(i);
            if (point.x < damagedTexture.getX()) damagedTexture.setX((int) point.x);
            else if (point.x > damagedTexture.getMaxX()) damagedTexture.setMaxX((int) point.x);
            if (point.y < damagedTexture.getY()) damagedTexture.setY((int) point.y);
            else if (point.y > damagedTexture.getMaxY()) damagedTexture.setMaxY((int) point.y);
        }
        if (damagedTexture.getX() < 0) damagedTexture.setX(0);
        if (damagedTexture.getY() < 0) damagedTexture.setY(0);
        if (damagedTexture.getMaxY() >= damageMask.getHeight()) damagedTexture.setMaxY(damageMask.getHeight() - 1);
        if (damagedTexture.getMaxX() >= damageMask.getWidth()) damagedTexture.setMaxX(damageMask.getWidth() - 1);
        return damagedTexture;
    }

    private ShipWreck createWreck(World world, double x, double y, double sin, double cos, float scaleX, float scaleY,
                                  org.locationtech.jts.geom.Polygon polygon, DamageMask damageMask, ShipData shipData,
                                  Coordinate localCenter) {
        RotationHelper.rotate((float) sin, (float) cos, (float) localCenter.x, (float) localCenter.y, rotatedLocalCenter);
        x += rotatedLocalCenter.x;
        y += rotatedLocalCenter.y;
        polygon.apply((CoordinateFilter) coordinate -> {
            coordinate.x -= localCenter.x;
            coordinate.y -= localCenter.y;
        });
        List<Shape> convexes = new ArrayList<>(32);
        decompose(polygon, convexes::add);
        return createWreck(world, x, y, sin, cos, scaleX, scaleY, convexes, polygon, damageMask, shipData, (float) localCenter.x,
                (float) localCenter.y);
    }

    private ShipWreck createWreck(World world, double x, double y, double sin, double cos, float scaleX, float scaleY,
                                  List<Shape> convexes, org.locationtech.jts.geom.Polygon polygon, DamageMask damageMask,
                                  ShipData shipData, float localOffsetX, float localOffsetY) {
        ShipWreck wreck = new ShipWreck((float) x, (float) y, (float) sin, (float) cos, scaleX, scaleY, shipData, damageMask,
                polygon, localOffsetX, localOffsetY);
        wreck.init(world, world.getNextId());
        for (int i = 0; i < convexes.size(); i++) {
            wreck.addHullFixture(wreck.setupFixture(new Fixture(convexes.get(i))));
        }

        return wreck;
    }

    public org.locationtech.jts.geom.Polygon createCirclePath(float x, float y, float sin, float cos, int count, float radius) {
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

    public static boolean isPolygonConnectedToContour(Vector2[] vertices, org.locationtech.jts.geom.Polygon polygon) {
        return isPolygonConnectedToContour(vertices, polygon, 0, 0);
    }

    public static boolean isPolygonConnectedToContour(Vector2[] vertices, org.locationtech.jts.geom.Polygon polygon, float offsetX,
                                                      float offsetY) {
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vertex = vertices[i];
            if (polygon.contains(GEOMETRY_FACTORY.createPoint(new Coordinate(vertex.x + offsetX, vertex.y + offsetY)))) {
                return true;
            }
        }

        return false;
    }
}