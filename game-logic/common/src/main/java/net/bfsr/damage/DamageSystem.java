package net.bfsr.damage;

import lombok.extern.log4j.Log4j2;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.event.damage.DamageEvent;
import net.bfsr.math.RotationHelper;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.joml.Vector2f;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.simplify.VWSimplifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Log4j2
public final class DamageSystem {
    public static final float CLIPPING_Y_OFFSET = -0.16f;
    public static final double CLIPPING_DELTA = 0.3f;
    private static final double MIN_AREA = 0.3;
    private static final float MIN_DISTANCE_BETWEEN_VERTICES_SQ = 0.3f;
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final SweepLine SWEEP_LINE = new SweepLine();

    private final Vector2f rotatedLocalCenter = new Vector2f();

    public void damage(DamageableRigidBody<?> damageable, float contactX, float contactY, Polygon clip, float radius, double x,
                       double y, double sin, double cos) {
        if (damageable.isDead()) {
            return;
        }

        World world = damageable.getWorld();
        DamageMask mask = damageable.getMask();
        mask.reset();
        damageable.getFixturesToAdd().clear();

        clipTexture(contactX, contactY, damageable, radius, mask, world.getRand());

        Polygon polygon = damageable.getPolygon();

        org.locationtech.jts.geom.Geometry difference = polygon.difference(clip);

        if (difference instanceof MultiPolygon multiPolygon) {
            processMultiPolygon(damageable, multiPolygon, x, y, sin, cos, true);
        } else if (difference instanceof Polygon polygon1) {
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_AREA) {
                org.locationtech.jts.geom.Geometry geometry = optimizeAndReverse(polygon1);
                if (geometry instanceof Polygon polygon2) {
                    clipTextureOutside(polygon2, mask, damageable.getSize());
                    damageable.setPolygon(polygon2);


                    decompose(polygon2, polygon3 -> damageable.getFixturesToAdd().add(damageable
                            .setupFixture(new BodyFixture(polygon3))));

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
            world.getEventBus().publish(new DamageEvent(damageable));
        }
    }

    private void processMultiPolygon(DamageableRigidBody<?> damageable, MultiPolygon multiPolygon, double x,
                                     double y, double sin, double cos, boolean optimize) {
        int polygonsCount = multiPolygon.getNumGeometries();
        double minDistance = Double.MAX_VALUE;
        Polygon newHull = null;
        List<Polygon> removedPaths = new ArrayList<>(polygonsCount - 1);
        for (int i = 0; i < polygonsCount; i++) {
            Polygon polygon1 = (Polygon) multiPolygon.getGeometryN(i);
            double area = Area.ofRing(polygon1.getExteriorRing().getCoordinateSequence());
            if (area > MIN_AREA) {
                if (optimize) {
                    org.locationtech.jts.geom.Geometry geometry = optimizeAndReverse(polygon1);

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
                            if (area > MIN_AREA) {
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
            decompose(newHull, polygon1 -> damageable.getFixturesToAdd().add(damageable
                    .setupFixture(new BodyFixture(polygon1))));

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

        Vector2f size = damageable.getSize();
        World world = damageable.getWorld();
        DamageMask mask = damageable.getMask();

        for (int i = 0; i < removedPaths.size(); i++) {
            Polygon removedPath = removedPaths.get(i);
            DamageMask damageMask = createInvertedDamageMask(removedPath, mask, size);
            ShipWreck wreck = createWreck(world, x, y, sin, cos, size.x, size.y, removedPath,
                    damageMask, (ShipData) damageable.getConfigData());
            wreck.getBody().setLinearVelocity(damageable.getBody().getLinearVelocity());
            wreck.getBody().setAngularVelocity(damageable.getBody().getAngularVelocity());
            world.getGameLogic().addFutureTask(() -> world.add(wreck));

            for (int j = 0; j < removedConnectedObjects.size(); j++) {
                ConnectedObject<?> connectedObject = removedConnectedObjects.get(j);
                if (connectedObject.isInside(removedPath)) {
                    wreck.addConnectedObject(connectedObject);
                } else {
                    connectedObject.spawn();
                }
            }
        }

        if (!damageable.isDead()) {
            clipTextureOutside(newHull, mask, size);
        }
    }

    private static org.locationtech.jts.geom.Geometry optimizeAndReverse(Polygon polygon) {
        int numInteriorRing = polygon.getNumInteriorRing();
        if (numInteriorRing > 0) {
            polygon = GEOMETRY_FACTORY.createPolygon(polygon.getExteriorRing());
        }

        return VWSimplifier.simplify(polygon, MIN_DISTANCE_BETWEEN_VERTICES_SQ).reverse();
    }

    private void fillTextureOutsidePolygon(List<Coordinate> coordinates, int count, DamageMask damageMask, byte value) {
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

    private List<Coordinate> clipTextureOutside(Polygon polygon, DamageMask mask, Vector2f scale) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = mask.getWidth() / scale.x;
        float localScaleY = mask.getHeight() / scale.y;

        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int size = coordinateSequence.size();
        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y += CLIPPING_Y_OFFSET;
        }

        org.locationtech.jts.geom.Geometry geometry = BufferOp.bufferOp(polygon, CLIPPING_DELTA,
                new BufferParameters(1, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_MITRE, 1.0));
        Coordinate[] coordinates = geometry.getCoordinates();

        List<Coordinate> res = new ArrayList<>(coordinates.length - 1);
        for (int i = 0, length = coordinates.length - 1; i < length; i++) {
            Coordinate coordinate = coordinates[i];
            res.add(new Coordinate((coordinate.x + sizeX) * localScaleX, (coordinate.y + sizeY) * localScaleY));
        }
        fillTextureOutsidePolygon(res, res.size(), mask, (byte) 0);

        for (int i = 0; i < size; i++) {
            Coordinate point64 = coordinateSequence.getCoordinate(i);
            point64.y -= CLIPPING_Y_OFFSET;
        }

        return res;
    }

    private void clipTexture(float x, float y, DamageableRigidBody<?> damageable, float clipRadius, DamageMask mask,
                             Random random) {
        Vector2f scale = damageable.getSize();
        float sin = (float) -damageable.getBody().getTransform().getSint();
        float cos = (float) damageable.getBody().getTransform().getCost();

        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        int width = mask.getWidth();
        int height = mask.getHeight();
        int radius = (int) (clipRadius * (width / scale.x) / 2.0f);

        float localPosX = x - (float) damageable.getBody().getTransform().getTranslationX();
        float localPosY = y - (float) damageable.getBody().getTransform().getTranslationY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = sin * localPosX + cos * localPosY;
        int localX = (int) ((rotatedX + sizeX) * (width / scale.x));
        int localY = (int) ((rotatedY + sizeY) * (height / scale.y));
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

    public static void decompose(Polygon polygon, Consumer<org.dyn4j.geometry.Polygon> polygonConsumer) {
        if (polygon.getExteriorRing().getNumPoints() > 4) {
            CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
            int count = coordinateSequence.size() - 1;
            Vector2[] vectors = new Vector2[count];
            for (int i = 0; i < count; i++) {
                Coordinate coordinate = coordinateSequence.getCoordinate(i);
                vectors[i] = new Vector2(coordinate.x, coordinate.y);
            }

            List<Convex> convexes = SWEEP_LINE.decompose(vectors);
            for (int i = 0; i < convexes.size(); i++) {
                polygonConsumer.accept((org.dyn4j.geometry.Polygon) convexes.get(i));
            }
        } else {
            CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
            Coordinate p0 = coordinateSequence.getCoordinate(0);
            Coordinate p1 = coordinateSequence.getCoordinate(1);
            Coordinate p2 = coordinateSequence.getCoordinate(2);
            polygonConsumer.accept(Geometry.createPolygon(new Vector2(p0.x, p0.y), new Vector2(p1.x, p1.y),
                    new Vector2(p2.x, p2.y)));
        }
    }

    private DamageMask createInvertedDamageMask(Polygon polygon, DamageMask damageMask, Vector2f scale) {
        DamageMask damagedTexture = new DamageMask(damageMask.getWidth(), damageMask.getHeight(), damageMask.copy());
        List<Coordinate> path = clipTextureOutside(polygon, damagedTexture, scale);
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
                                  Polygon polygon, DamageMask damageMask, ShipData shipData) {
        Coordinate localCenter = polygon.getCentroid().getCoordinate();
        RotationHelper.rotate((float) sin, (float) cos, (float) localCenter.x, (float) localCenter.y, rotatedLocalCenter);
        x += rotatedLocalCenter.x;
        y += rotatedLocalCenter.y;
        polygon.apply((CoordinateFilter) coordinate -> {
            coordinate.x -= localCenter.x;
            coordinate.y -= localCenter.y;
        });
        List<Convex> convexes = new ArrayList<>(32);
        decompose(polygon, convexes::add);
        return createWreck(world, x, y, sin, cos, scaleX, scaleY, convexes, polygon, damageMask, shipData, (float) localCenter.x,
                (float) localCenter.y);
    }

    private ShipWreck createWreck(World world, double x, double y, double sin, double cos, float scaleX, float scaleY,
                                  List<Convex> convexes, Polygon polygon, DamageMask damageMask, ShipData shipData,
                                  float localOffsetX, float localOffsetY) {
        ShipWreck wreck = new ShipWreck((float) x, (float) y, (float) sin, (float) cos, scaleX, scaleY, shipData, damageMask,
                polygon, localOffsetX, localOffsetY);
        wreck.init(world, world.getNextId());
        Body body = wreck.getBody();

        for (int i = 0; i < convexes.size(); i++) {
            body.addFixture(wreck.setupFixture(new BodyFixture(convexes.get(i))));
        }

        body.setMass(MassType.NORMAL);

        return wreck;
    }

    public Polygon createCirclePath(float x, float y, float sin, float cos, int count, float radius) {
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

    public static boolean isPolygonConnectedToContour(Vector2[] vertices, Polygon polygon) {
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vertex = vertices[i];
            if (polygon.contains(GEOMETRY_FACTORY.createPoint(new Coordinate(vertex.x, vertex.y)))) {
                return true;
            }
        }

        return false;
    }
}