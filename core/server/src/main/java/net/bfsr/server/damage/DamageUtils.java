package net.bfsr.server.damage;

import clipper2.Clipper;
import clipper2.core.*;
import clipper2.engine.ClipperD;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import earcut4j.Earcut;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.wreck.ShipWreckDamagable;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DamageUtils {
    private static final SweepLine SWEEP_LINE = new SweepLine();
    private static final Earcut EARCUT = new Earcut();
    public static final double SCALE = 10000.0;
    public static final double INV_SCALE = 1 / SCALE;
    private static final double MIN_WRECK_AREA = 0.15;
    private static final double MIN_SHIP_AREA = 0.75;
    private static final ClipperOffset CLIPPER_OFFSET = new ClipperOffset(2.0);
    static Path64 path = new Path64();
    static Vector2 CACHED_VECTOR = new Vector2();
    static List<PathD> holes = new ArrayList<>(8);
    static Paths64 difference64 = new Paths64();
    static ClipperD clipper = new ClipperD();

    public static void damage(Damagable damagable, double contactX, double contactY, Path64 clip, float radius) {
        if (damagable.isDead() || damagable.getContours().size() == 0) return;

        DamageMask mask = damagable.getMask();
        Body body = damagable.getBody();
        double x = (float) body.getTransform().getTranslationX();
        double y = (float) body.getTransform().getTranslationY();
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();
        Vector2f scale = damagable.getScale();
        mask.reset();
        clipTexture(contactX, contactY, damagable, radius, mask);

        try {
            PathsD contours = damagable.getContours();

            clipper.Clear();
            for (int i = 0; i < contours.size(); i++) {
                PathD pathD = contours.get(i);
                Path64 scaledPath = new Path64(pathD.size());
                for (int i1 = 0; i1 < pathD.size(); i1++) {
                    PointD point = pathD.get(i1);
                    scaledPath.add(new Point64(point, SCALE));
                }
                clipper.AddPath(scaledPath, PathType.SUBJECT);
            }
            clipper.AddPath(clip, PathType.CLIP);
            clipper.executeDifference(FillRule.EvenOdd, difference64);

            damagable.getFixturesToAdd().clear();
            contours.clear();
            if (difference64.size() > 1) {
                double minDistance = Double.MAX_VALUE;
                holes.clear();
                PathD newHull = null;
                Path64 newHull64 = null;
                List<PathD> removedPaths = new ArrayList<>(difference64.size() - 1);
                List<Path64> removedPaths64 = new ArrayList<>(difference64.size() - 1);
                for (int i = 0; i < difference64.size(); i++) {
                    Path64 path64 = difference64.get(i);
                    PathD pathD = new PathD(path64.size());
                    for (int i1 = 0; i1 < path64.size(); i1++) {
                        Point64 point64 = path64.get(i1);
                        pathD.add(new PointD(point64, INV_SCALE));
                    }

                    if (Clipper.Area(pathD) > 0) {
                        Vector2 pathCenter = getPathCenter(pathD);
                        double distance = pathCenter.distanceSquared(0, 0);
                        if (distance < minDistance) {
                            if (newHull != null) {
                                removedPaths.add(newHull);
                                removedPaths64.add(newHull64);
                            }
                            newHull = pathD;
                            newHull64 = path64;
                            minDistance = distance;
                        } else {
                            removedPaths.add(pathD);
                            removedPaths64.add(path64);
                        }
                    } else {
                        holes.add(pathD);
                    }
                }

                double area = Clipper.Area(newHull);
                if (area > MIN_SHIP_AREA) {
                    contours.add(newHull);

                    for (int i = 0; i < holes.size(); i++) {
                        PathD pathD = holes.get(i);
                        if (InternalClipper.PolygonInPolygon(pathD, newHull)) {
                            contours.add(pathD);
                            if (removedPaths.size() > 0) {
                                holes.remove(i--);
                            }
                        }
                    }

                    if (contours.size() > 1) {
                        earCut(damagable, contours);
                    } else {
                        Vector2[] vector2List = new Vector2[newHull.size()];
                        for (int i1 = 0; i1 < newHull.size(); i1++) {
                            PointD pointD = newHull.get(i1);
                            vector2List[i1] = new Vector2(pointD.x, pointD.y);
                        }

                        try {
                            if (vector2List.length > 3) {
                                addFixtures(damagable, SWEEP_LINE.decompose(vector2List));
                            } else {
                                addFixture(damagable, Geometry.createPolygon(vector2List));
                            }
                        } catch (Exception e) {
                            System.out.println("Error during decompose " + e.getMessage() + " Area: " + area);
                            for (int i = 0; i < vector2List.length; i++) {
                                System.out.println(vector2List[i].x + " " + vector2List[i].y);
                            }
                        }
                    }
                } else {
                    damagable.destroy();
                }

                for (int i = 0; i < removedPaths.size(); i++) {
                    PathD removedPath = removedPaths.get(i);
                    area = Clipper.Area(removedPath);
                    if (area > MIN_WRECK_AREA) {
                        DamageMask damageMask = createInvertedDamageMask(removedPaths64.get(i), mask, scale);
                        ShipWreckDamagable damage = createDamage(x, y, sin, cos, scale.x, scale.y, removedPath, damageMask, damagable.getTextureIndex());
                        if (damage != null) {
                            damage.getBody().setLinearVelocity(damagable.getBody().getLinearVelocity());
                            damage.getBody().setAngularVelocity(damagable.getBody().getAngularVelocity());
                            Server.getInstance().getWorld().addDamage(damage);
                            damage.sendSpawnPacket();
                        }
                    }
                }

                if (!damagable.isDead()) {
                    clipTextureOutside(newHull64, mask, scale);
                }
            } else if (difference64.size() > 0) {
                Path64 path64 = difference64.get(0);
                PathD path = new PathD(path64.size());
                for (int i1 = 0; i1 < path64.size(); i1++) {
                    Point64 point64 = path64.get(i1);
                    path.add(new PointD(point64, INV_SCALE));
                }

                double area = Clipper.Area(path);
                if (area > MIN_SHIP_AREA) {
                    contours.add(path);
                    clipTextureOutside(path64, mask, scale);
                    Vector2[] vector2List = new Vector2[path.size()];
                    for (int i1 = 0; i1 < path.size(); i1++) {
                        PointD pointD = path.get(i1);
                        vector2List[i1] = new Vector2(pointD.x, pointD.y);
                    }
                    try {
                        if (vector2List.length > 3) {
                            try {
                                addFixtures(damagable, SWEEP_LINE.decompose(vector2List));
                            } catch (Exception e) {
                                System.out.println("Error during decompose " + e.getMessage() + " Area: " + area);
                                for (int i = 0; i < vector2List.length; i++) {
                                    System.out.println(vector2List[i].x + " " + vector2List[i].y);
                                }
                            }
                        } else {
                            addFixture(damagable, Geometry.createPolygon(vector2List));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    damagable.destroy();
                }
            } else {
                damagable.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!damagable.isDead() && damagable.getContours().size() > 0 && mask.dirty()) {
            damagable.sync();
        }
    }

    private static void earCut(Damagable damagable, PathsD contours) {
        EARCUT.execute(contours, polygon -> addFixture(damagable, polygon));
    }

    private static void addFixtures(Damagable damagable, List<Convex> convexes) {
        for (int i = 0; i < convexes.size(); i++) {
            addFixture(damagable, convexes.get(i));
        }
    }

    private static void addFixture(Damagable shipDamagable, Convex convex) {
        BodyFixture bodyFixture = new BodyFixture(convex);
        shipDamagable.setupFixture(bodyFixture);
        shipDamagable.getFixturesToAdd().add(bodyFixture);
    }

    private static void fillTextureOutsidePolygon(PathD pathD, DamageMask damageMask, byte value) {
        int size = pathD.size();
        int[] nodeX = new int[size];
        int nodes, i, j, swap, pixelX;
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        byte[] data = damageMask.getData();

        for (int pixelY = 0; pixelY < damageMask.getHeight(); pixelY++) {
            nodes = 0;
            j = size - 1;
            for (i = 0; i < size; i++) {
                PointD pointD = pathD.get(i);
                PointD pointD1 = pathD.get(j);
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

    private static void fillTextureByPolygon(PathD pathD, DamageMask mask, byte value) {
        int size = pathD.size();
        int[] nodeX = new int[size];
        int nodes, swap, i, j, pixelX;
        byte[] data = mask.getData();

        for (int pixelY = 0; pixelY < mask.getHeight(); pixelY++) {
            nodes = 0;
            j = size - 1;
            for (i = 0; i < size; i++) {
                PointD pointD = pathD.get(i);
                PointD pointD1 = pathD.get(j);
                if (pointD.y < pixelY && pointD1.y >= pixelY || pointD1.y < pixelY && pointD.y >= pixelY) {
                    nodeX[nodes++] = (int) (pointD.x + (pixelY - pointD.y) / (pointD1.y - pointD.y) * (pointD1.x - pointD.x));
                }
                j = i;
            }

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

                for (i = 0; i < nodes; i += 2) {
                    for (pixelX = nodeX[i]; pixelX < nodeX[i + 1]; pixelX++) {
                        data[pixelY * mask.getHeight() + pixelX] = value;
                    }
                }
            }
        }
    }

    public static void clipTextureOutside(Path64 path, DamageMask mask, Vector2f scale) {
        clipTextureOutside(path, mask, scale, 1.2 * SCALE);
    }

    public static PathD clipTextureOutside(Path64 path, DamageMask mask, Vector2f scale, double offset) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = mask.getWidth() / scale.x;
        float localScaleY = mask.getHeight() / scale.y;

        CLIPPER_OFFSET.Clear();
        CLIPPER_OFFSET.AddPath(path, JoinType.Miter, EndType.Polygon);
        Paths64 paths = CLIPPER_OFFSET.Execute(offset / (localScaleX * 0.5f));
        try {
            Path64 path64 = paths.get(0);
            PathD res = new PathD(path.size());
            for (int i = 0, path64Size = path64.size(); i < path64Size; i++) {
                Point64 pt = path64.get(i);
                res.add(new PointD((pt.x * INV_SCALE + sizeX) * localScaleX, (pt.y * INV_SCALE + sizeY) * localScaleY));
            }
            fillTextureOutsidePolygon(res, mask, (byte) 0);
            return res;
        } catch (Exception e) {
            System.out.println("Erorr during clip texture outside " + e.getMessage());
            for (int i = 0; i < path.size(); i++) {
                Point64 pointD = path.get(i);
                System.out.println("x: " + pointD.x * INV_SCALE + ", y: " + pointD.y * INV_SCALE);
            }
            e.printStackTrace();
            return null;
        }
    }

    public static void clipTexture(Path64 path, DamageMask mask, Vector2f scale) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = mask.getWidth() / scale.x;
        float localScaleY = mask.getHeight() / scale.y;

        CLIPPER_OFFSET.Clear();
        CLIPPER_OFFSET.AddPath(path, JoinType.Miter, EndType.Polygon);
        Path64 result = CLIPPER_OFFSET.Execute(22.0 / localScaleX).get(0);

        int cnt = path.size();
        PathD res = new PathD(cnt);
        for (int i = 0, path64Size = result.size(); i < path64Size; i++) {
            Point64 pt = result.get(i);
            res.add(new PointD((pt.x * INV_SCALE + sizeX) * localScaleX, (pt.y * INV_SCALE + sizeY) * localScaleY));
        }

        fillTextureByPolygon(res, mask, (byte) 0);
    }

    public static void clipTexture(double x, double y, Damagable damagable, float clipRadius, DamageMask mask) {
        Vector2f scale = damagable.getScale();
        double sin = -damagable.getBody().getTransform().getSint();
        double cos = damagable.getBody().getTransform().getCost();

        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        int width = mask.getWidth();
        int height = mask.getHeight();
        int radius = (int) (clipRadius * (width / scale.x) / 2.0f);

        double localPosX = x - damagable.getBody().getTransform().getTranslationX();
        double localPosY = y - damagable.getBody().getTransform().getTranslationY();
        double rotatedX = cos * localPosX - sin * localPosY;
        double rotatedY = sin * localPosX + cos * localPosY;
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
                    float square = (dx * dx + dy * dy) * (Server.getInstance().getWorld().getRand().nextFloat(0.5f) + 0.5f);
                    if (square < radiusSq) {
                        int index = j * height + i;
                        float holeThreshold = radiusSq / 4.0f * Server.getInstance().getWorld().getRand().nextFloat();
                        if (square <= holeThreshold) {
                            data[index] = value;
                        } else {
                            data[index] = (byte) Math.min((int) (((square - holeThreshold) / radiusSq) * 255), Byte.toUnsignedInt(data[index]));
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

    public static DamageMask createInvertedDamageMask(Path64 hull, DamageMask damageMask, Vector2f scale) {
        DamageMask damagedTexture = new DamageMask(damageMask.getWidth(), damageMask.getHeight(), damageMask.copy());
        PathD path = clipTextureOutside(hull, damagedTexture, scale, SCALE);
        for (int i = 0; i < path.size(); i++) {
            PointD point = path.get(i);
            if (point.x < damagedTexture.getX()) damagedTexture.setX((int) point.x);
            else if (point.x > damagedTexture.getMaxX()) damagedTexture.setMaxX((int) point.x);
            if (point.y < damagedTexture.getY()) damagedTexture.setY((int) point.y);
            else if (point.y > damagedTexture.getMaxY()) damagedTexture.setMaxY((int) point.y);
        }
        if (damagedTexture.getX() < 0) damagedTexture.setX(0);
        if (damagedTexture.getY() < 0) damagedTexture.setY(0);
        if (damagedTexture.getMaxY() >= damageMask.getHeight()) {
            damagedTexture.setMaxY(damageMask.getHeight() - 1);
        }
        if (damagedTexture.getMaxX() >= damageMask.getWidth()) {
            damagedTexture.setMaxX(damageMask.getWidth() - 1);
        }
        return damagedTexture;
    }

    private static ShipWreckDamagable createDamage(double x, double y, double sin, double cos, float scaleX, float scaleY, PathD contour, DamageMask damageMask, int textureIndex) {
        Vector2[] vectors = new Vector2[contour.size()];
        for (int i = 0; i < vectors.length; i++) {
            PointD pointD = contour.get(i);
            vectors[i] = new Vector2(pointD.x, pointD.y);
        }

        PathsD contours = new PathsD();
        contours.add(contour);

        if (vectors.length > 3) {
            try {
                return createDamage(x, y, sin, cos, scaleX, scaleY, SWEEP_LINE.decompose(vectors), contours, damageMask, textureIndex);
            } catch (Exception e) {
                System.out.println("Error during decompose " + e.getMessage());
                for (int i = 0; i < vectors.length; i++) {
                    System.out.println(vectors[i].x + "" + vectors[i].y);
                }
                return null;
            }
        } else {
            return createDamage(x, y, sin, cos, scaleX, scaleY, Collections.singletonList(Geometry.createPolygon(vectors)), contours, damageMask, textureIndex);
        }
    }

    private static ShipWreckDamagable createDamage(double x, double y, double sin, double cos, float scaleX, float scaleY, List<Convex> convexes, PathsD contours,
                                                   DamageMask damageMask, int textureIndex) {
        ShipWreckDamagable shipWreckDamagable = new ShipWreckDamagable((float) x, (float) y, (float) sin, (float) cos, scaleX, scaleY, textureIndex, damageMask, contours);
        Body body = shipWreckDamagable.getBody();

        for (int i = 0; i < convexes.size(); i++) {
            BodyFixture bodyFixture = new BodyFixture(convexes.get(i));
            shipWreckDamagable.setupFixture(bodyFixture);
            body.addFixture(bodyFixture);
        }

        body.setMass(MassType.NORMAL);
        body.setUserData(shipWreckDamagable);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.01f);

        shipWreckDamagable.init();

        return shipWreckDamagable;
    }

    private static Vector2 getPathCenter(PathD pathD) {
        double x = 0;
        double y = 0;
        for (int i = 0, size = pathD.size(); i < size; i++) {
            PointD pointD = pathD.get(i);
            x += pointD.x;
            y += pointD.y;
        }

        CACHED_VECTOR.set(x / pathD.size(), y / pathD.size());
        return CACHED_VECTOR;
    }

    public static Path64 createCirclePath(double dx, double dy, double sin, double cos, int count, float radius) {
        final double pin = Geometry.TWO_PI / count;

        final double c = Math.cos(pin);
        final double s = Math.sin(pin);
        double t;

        double vertexX = radius;
        double vertexY = 0;

        if (path.size() < count) {
            int countToAdd = count - path.size();
            for (int i = 0; i < countToAdd; i++) {
                path.add(new Point64());
            }
        } else if (path.size() > count) {
            while (path.size() > count) {
                path.remove(0);
            }
        }

        for (int i = 0; i < count; i++) {
            double localPosX = vertexX + dx;
            double localPosY = vertexY + dy;
            double newX = cos * localPosX - sin * localPosY;
            double newY = sin * localPosX + cos * localPosY;
            Point64 pointD = path.get(i);
            pointD.x = (long) Math.rint(newX * SCALE);
            pointD.y = (long) Math.rint(newY * SCALE);

            t = vertexX;
            vertexX = c * vertexX - s * vertexY;
            vertexY = s * t + c * vertexY;
        }

        return path;
    }
}