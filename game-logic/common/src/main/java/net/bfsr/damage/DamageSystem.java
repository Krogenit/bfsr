package net.bfsr.damage;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.entity.damageable.DamageableRigidBodyConfigData;
import net.bfsr.engine.geometry.GeometryUtils;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.World;
import net.bfsr.entity.wreck.ShipWreck;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.simplify.VWSimplifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public final class DamageSystem {
    private static final boolean DEBUG = false;
    private static final double MIN_POLYGON_AREA = 0.3;
    public static final BufferParameters BUFFER_PARAMETERS = new BufferParameters(1, BufferParameters.CAP_SQUARE,
            BufferParameters.JOIN_MITRE, 0.1);

    private final Vector2f rotatedLocalCenter = new Vector2f();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public void damage(DamageableRigidBody rigidBody, float contactX, float contactY, Polygon clip, float maskClipRadius, float x, float y,
                       float sin, float cos, Runnable onDamageSuccessRunnable) {
        if (rigidBody.isDead()) {
            return;
        }

        DamageMask mask = rigidBody.getDamageMask();
        mask.reset();
        rigidBody.removeHullFixtures();

        clipMask(contactX, contactY, -sin, cos, rigidBody, maskClipRadius, mask, rigidBody.getLocalOffsetX(), rigidBody.getLocalOffsetY());

        Polygon polygon = rigidBody.getPolygon();

        Geometry difference = polygon.difference(clip);
        processDifference(difference, rigidBody, x, y, sin, cos, mask, true, true);

        if (!rigidBody.isDead() && mask.dirty()) {
            onDamageSuccessRunnable.run();
        }
    }

    private void processDifference(Geometry difference, DamageableRigidBody rigidBody, float x, float y, float sin, float cos,
                                   DamageMask mask, boolean destroyEntity, boolean reconstructHull) {
        if (difference instanceof MultiPolygon multiPolygon) {
            processMultiPolygon(rigidBody, multiPolygon, x, y, sin, cos, mask, true, destroyEntity, reconstructHull);
        } else if (difference instanceof Polygon polygon1) {
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_POLYGON_AREA) {
                DamageableRigidBodyConfigData configData = rigidBody.getConfigData();
                Geometry geometry = optimizeAndReverse(polygon1, configData.getMinDistanceBetweenVerticesSq());
                if (geometry instanceof Polygon polygon2) {
                    clipMaskOutside(polygon2, mask, rigidBody.getSizeX(), rigidBody.getSizeY(), rigidBody.getLocalOffsetX(),
                            rigidBody.getLocalOffsetY(), configData.getBufferDistance(), configData.getBufferYOffset());
                    if (reconstructHull) {
                        rigidBody.setPolygon(polygon2);
                        GeometryUtils.decompose(polygon2,
                                polygon3 -> rigidBody.addHullFixture(rigidBody.setupFixture(new Fixture(polygon3))));

                        List<ConnectedObject<?>> connectedObjects = rigidBody.getConnectedObjects();
                        for (int i = 0; i < connectedObjects.size(); i++) {
                            ConnectedObject<?> connectedObject = connectedObjects.get(i);
                            if (!connectedObject.isInside(polygon2)) {
                                rigidBody.removeConnectedObject(i--);
                                connectedObject.spawn();
                            }
                        }

                        rigidBody.onContourReconstructed(polygon2);
                    } else {
                        float sizeX = rigidBody.getSizeX();
                        float sizeY = rigidBody.getSizeY();
                        createWrecksWithPolygons(Collections.singletonList(polygon2), rigidBody, x, y, sin, cos, sizeX, sizeY, configData,
                                mask, rigidBody.getConnectedObjects());
                    }
                } else if (geometry instanceof MultiPolygon multiPolygon) {
                    processMultiPolygon(rigidBody, multiPolygon, x, y, sin, cos, mask, false, destroyEntity, reconstructHull);
                } else {
                    if (destroyEntity) {
                        rigidBody.setDead();
                    }
                }
            } else {
                if (destroyEntity) {
                    rigidBody.setDead();
                }
            }
        } else {
            throw new IllegalStateException("Unsupported geometry type " + difference);
        }
    }

    public void createDestroyedShipWrecks(DamageableRigidBody rigidBody) {
        float sizeX = rigidBody.getSizeX();
        float sizeY = rigidBody.getSizeY();
        float maxSize = Math.max(sizeX, sizeY);
        float polygonWidth = maxSize * 1.5f;
        float polygonHeight = maxSize * 0.1f;

        float randomAngle = RandomHelper.randomFloat(random, 0.0f, MathUtils.TWO_PI);

        Polygon clipPolygon = GeometryUtils.createCenteredRhombusPolygon(polygonWidth, polygonHeight, 0.0f, 0.0f, LUT.sin(randomAngle),
                LUT.cos(randomAngle));

        Polygon polygon = rigidBody.getPolygon();
        Geometry difference = polygon.difference(clipPolygon);
        processDifference(difference, rigidBody, rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(),
                rigidBody.getDamageMask(), false, false);
    }

    private void processMultiPolygon(DamageableRigidBody rigidBody, MultiPolygon multiPolygon, float x, float y, float sin, float cos,
                                     DamageMask mask, boolean optimize, boolean destroyEntity, boolean reconstructHull) {
        int polygonsCount = multiPolygon.getNumGeometries();
        double minDistance = Double.MAX_VALUE;
        Polygon newHull = null;
        List<Polygon> removedPaths = new ArrayList<>(polygonsCount - 1);
        DamageableRigidBodyConfigData configData = rigidBody.getConfigData();
        for (int i = 0; i < polygonsCount; i++) {
            Polygon polygon1 = (Polygon) multiPolygon.getGeometryN(i);
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_POLYGON_AREA) {
                if (optimize) {
                    Geometry geometry = optimizeAndReverse(polygon1, configData.getMinDistanceBetweenVerticesSq());

                    if (geometry instanceof Polygon polygon2) {
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
                            Polygon polygon2 = (Polygon) multiPolygon1.getGeometryN(j);
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

        List<ConnectedObject<?>> removedConnectedObjects;
        if (newHull != null) {
            if (reconstructHull) {
                rigidBody.setPolygon(newHull);
                GeometryUtils.decompose(newHull, polygon1 -> rigidBody.addHullFixture(rigidBody.setupFixture(new Fixture(polygon1))));

                List<ConnectedObject<?>> connectedObjects = rigidBody.getConnectedObjects();
                removedConnectedObjects = new ArrayList<>(connectedObjects.size());
                for (int i = 0; i < connectedObjects.size(); i++) {
                    ConnectedObject<?> connectedObject = connectedObjects.get(i);
                    if (!connectedObject.isInside(newHull)) {
                        rigidBody.removeConnectedObject(i--);
                        removedConnectedObjects.add(connectedObject);
                    }
                }

                rigidBody.onContourReconstructed(newHull);
            } else {
                removedPaths.add(newHull);
                removedConnectedObjects = rigidBody.getConnectedObjects();
            }
        } else {
            removedConnectedObjects = Collections.emptyList();
        }

        float sizeX = rigidBody.getSizeX();
        float sizeY = rigidBody.getSizeY();
        createWrecksWithPolygons(removedPaths, rigidBody, x, y, sin, cos, sizeX, sizeY, configData, mask, removedConnectedObjects);

        for (int i = 0; i < removedConnectedObjects.size(); i++) {
            removedConnectedObjects.get(i).spawn();
        }

        if (newHull != null) {
            clipMaskOutside(newHull, mask, sizeX, sizeY, rigidBody.getLocalOffsetX(), rigidBody.getLocalOffsetY(),
                    configData.getBufferDistance(), configData.getBufferYOffset());
        } else {
            if (destroyEntity) {
                rigidBody.setDead();
            }
        }
    }

    private void createWrecksWithPolygons(List<Polygon> polygons, DamageableRigidBody rigidBody, float x, float y, float sin, float cos,
                                          float sizeX, float sizeY, DamageableRigidBodyConfigData configData, DamageMask mask,
                                          List<ConnectedObject<?>> removedConnectedObjects) {
        World world = rigidBody.getWorld();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            DamageMask damageMask = createInvertedDamageMask(polygon, mask, sizeX, sizeY, configData.getBufferDistance(),
                    configData.getBufferYOffset());
            Coordinate localCenter = polygon.getCentroid().getCoordinate();
            ShipWreck wreck = createWreck(world, x, y, sin, cos, sizeX, sizeY, polygon,
                    damageMask, configData, localCenter);
            wreck.setLinearVelocity(rigidBody.getLinearVelocity());
            wreck.setAngularVelocity(rigidBody.getAngularVelocity());

            for (int j = 0; j < removedConnectedObjects.size(); j++) {
                ConnectedObject<?> connectedObject = removedConnectedObjects.get(j);
                float offsetX = (float) -localCenter.x;
                float offsetY = (float) -localCenter.y;
                if (connectedObject.isInside(polygon, offsetX, offsetY)) {
                    wreck.addConnectedObject(connectedObject);
                    connectedObject.addPositionOffset(offsetX, offsetY);
                    removedConnectedObjects.remove(j--);
                }
            }

            world.add(wreck);
        }
    }

    public static Geometry optimizeAndReverse(Polygon polygon, float minDistanceBetweenVerticesSq) {
        int numInteriorRing = polygon.getNumInteriorRing();
        if (numInteriorRing > 0) {
            polygon = GeometryUtils.createPolygon(polygon.getExteriorRing());
        }

        return VWSimplifier.simplify(polygon, minDistanceBetweenVerticesSq).reverse();
    }

    public static void fillMaskOutsidePolygon(List<Coordinate> coordinates, int count, DamageMask damageMask, byte value) {
        int[] nodeX = new int[count];
        int nodes, i, j, swap, pixelX;
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        byte[] data = damageMask.getData();
        int damageMaskWidth = damageMask.getWidth();
        int damageMaskHeight = damageMask.getHeight();

        if (DEBUG) {
            for (int k = 0; k < damageMaskHeight; k++) {
                for (int n = 0; n < damageMaskWidth; n++) {
                    log.debug(Byte.toUnsignedInt(data[k * damageMaskWidth + n]) == 255 ? 1 : 0);
                }

                log.debug("");
            }
        }

        for (int pixelY = 0; pixelY < damageMaskHeight; pixelY++) {
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
                        int index = pixelY * damageMaskWidth + pixelX;
                        byte currValue = data[index];
                        if (currValue != value) {
                            data[index] = value;
                            onePixelPut = true;

                            if (pixelX < x) x = pixelX;
                            if (pixelX > maxX) maxX = pixelX;
                        }
                    }
                    startX = nodeX[i] + 1;
                    endX = i + 1 == nodes ? damageMaskWidth : nodeX[i + 1];
                }

                for (pixelX = startX; pixelX < damageMaskWidth; pixelX++) {
                    int index = pixelY * damageMaskWidth + pixelX;
                    byte currValue = data[index];
                    if (currValue != value) {
                        data[index] = value;
                        onePixelPut = true;

                        if (pixelX < x) x = pixelX;
                        if (pixelX > maxX) maxX = pixelX;
                    }
                }
            } else {
                for (pixelX = 0; pixelX < damageMaskWidth; pixelX++) {
                    int index = pixelY * damageMaskWidth + pixelX;
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

                if (DEBUG) {
                    log.debug("Change in pixelY {}", pixelY);

                    for (int k = 0; k < damageMaskHeight; k++) {
                        for (int n = 0; n < damageMaskWidth; n++) {
                            log.debug(Byte.toUnsignedInt(data[k * damageMaskWidth + n]) == 255 ? 1 : 0);
                        }

                        log.debug("");
                    }
                }
            } else if (DEBUG) {
                log.debug("No changes in pixelY {}", pixelY);
            }
        }

        damageMask.setX(Math.min(damageMask.getX(), x));
        damageMask.setY(Math.min(damageMask.getY(), y));
        damageMask.setMaxX(Math.max(damageMask.getMaxX(), maxX));
        damageMask.setMaxY(Math.max(damageMask.getMaxY(), maxY));

        if (DEBUG) {
            log.debug("");
            log.debug("---- AFTER ----");
            log.debug("");

            for (int k = 0; k < damageMaskHeight; k++) {
                for (int n = 0; n < damageMaskWidth; n++) {
                    log.debug(Byte.toUnsignedInt(data[k * damageMaskWidth + n]) == 255 ? 1 : 0);
                }

                log.debug("");
            }
        }
    }

    private static List<Coordinate> clipMaskOutside(Polygon polygon, DamageMask mask, float sizeX, float sizeY, float offsetX,
                                                    float offsetY, float bufferDistance, float bufferYOffset) {
        float halfSizeX = sizeX / 2.0f;
        float halfSizeY = sizeY / 2.0f;
        float localScaleX = mask.getWidth() / sizeX;
        float localScaleY = mask.getHeight() / sizeY;

        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int size = coordinateSequence.size();
        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y += bufferYOffset;
        }

        Polygon polygon1 = (Polygon) BufferOp.bufferOp(polygon, bufferDistance,
                BUFFER_PARAMETERS);

        CoordinateSequence coordinateSequence1 = polygon1.getExteriorRing().getCoordinateSequence();
        List<Coordinate> res = new ArrayList<>(coordinateSequence1.size() - 1);
        for (int i = 0, length = coordinateSequence1.size() - 1; i < length; i++) {
            Coordinate coordinate = coordinateSequence1.getCoordinate(i);
            res.add(new Coordinate((coordinate.x + halfSizeX + offsetX) * localScaleX, (coordinate.y + halfSizeY + offsetY) * localScaleY));
        }
        fillMaskOutsidePolygon(res, res.size(), mask, (byte) 0);

        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y -= bufferYOffset;
        }

        return res;
    }

    private void clipMask(float x, float y, float sin, float cos, DamageableRigidBody damageable, float clipRadius, DamageMask mask,
                          float localOffsetX, float localOffsetY) {
        float sizeX = damageable.getSizeX();
        float sizeY = damageable.getSizeY();
        float halfSizeX = sizeX * 0.5f;
        float halfSizeY = sizeY * 0.5f;
        int width = mask.getWidth();
        int height = mask.getHeight();
        int radius = (int) Math.ceil(clipRadius * (width / sizeX) * 0.5f);

        float localPosX = x - damageable.getX();
        float localPosY = y - damageable.getY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = cos * localPosY + sin * localPosX;
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
                        int index = j * width + i;
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

    private DamageMask createInvertedDamageMask(Polygon polygon, DamageMask damageMask, float sizeX, float sizeY, float bufferDistance,
                                                float bufferYOffset) {
        DamageMask invertexDamageMask = new DamageMask(damageMask);

        List<Coordinate> path = clipMaskOutside(polygon, invertexDamageMask, sizeX, sizeY, 0, 0, bufferDistance, bufferYOffset);

        for (int i = 0; i < path.size(); i++) {
            Coordinate point = path.get(i);

            if (point.x < invertexDamageMask.getX()) {
                invertexDamageMask.setX((int) point.x);
            } else if (point.x > invertexDamageMask.getMaxX()) {
                invertexDamageMask.setMaxX((int) point.x);
            }

            if (point.y < invertexDamageMask.getY()) {
                invertexDamageMask.setY((int) point.y);
            } else if (point.y > invertexDamageMask.getMaxY()) {
                invertexDamageMask.setMaxY((int) point.y);
            }
        }

        if (invertexDamageMask.getX() < 0) {
            invertexDamageMask.setX(0);
        }

        if (invertexDamageMask.getY() < 0) {
            invertexDamageMask.setY(0);
        }

        if (invertexDamageMask.getMaxY() >= damageMask.getHeight()) {
            invertexDamageMask.setMaxY(damageMask.getHeight() - 1);
        }

        if (invertexDamageMask.getMaxX() >= damageMask.getWidth()) {
            invertexDamageMask.setMaxX(damageMask.getWidth() - 1);
        }

        return invertexDamageMask;
    }

    private ShipWreck createWreck(World world, double x, double y, double sin, double cos, float scaleX, float scaleY, Polygon polygon,
                                  DamageMask damageMask, DamageableRigidBodyConfigData configData, Coordinate localCenter) {
        RotationHelper.rotate((float) sin, (float) cos, (float) localCenter.x, (float) localCenter.y, rotatedLocalCenter);
        x += rotatedLocalCenter.x;
        y += rotatedLocalCenter.y;
        polygon.apply((CoordinateFilter) coordinate -> {
            coordinate.x -= localCenter.x;
            coordinate.y -= localCenter.y;
        });
        List<Shape> convexes = new ArrayList<>(32);
        GeometryUtils.decompose(polygon, convexes::add);
        return createWreck(world, x, y, sin, cos, scaleX, scaleY, convexes, polygon, damageMask, configData, (float) localCenter.x,
                (float) localCenter.y);
    }

    private ShipWreck createWreck(World world, double x, double y, double sin, double cos, float scaleX, float scaleY, List<Shape> convexes,
                                  Polygon polygon, DamageMask damageMask, DamageableRigidBodyConfigData configData, float localOffsetX,
                                  float localOffsetY) {
        ShipWreck wreck = new ShipWreck((float) x, (float) y, (float) sin, (float) cos, scaleX, scaleY, configData, damageMask,
                polygon, localOffsetX, localOffsetY);
        wreck.init(world, world.getNextId());
        for (int i = 0; i < convexes.size(); i++) {
            wreck.addHullFixture(wreck.setupFixture(new Fixture(convexes.get(i))));
        }

        return wreck;
    }

    public static boolean isPolygonConnectedToContour(Vector2[] vertices, Polygon polygon) {
        return isPolygonConnectedToContour(vertices, polygon, 0, 0);
    }

    public static boolean isPolygonConnectedToContour(Vector2[] vertices, Polygon polygon, float offsetX, float offsetY) {
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vertex = vertices[i];
            if (polygon.contains(GeometryUtils.createPoint(new Coordinate(vertex.x + offsetX, vertex.y + offsetY)))) {
                return true;
            }
        }

        return false;
    }
}