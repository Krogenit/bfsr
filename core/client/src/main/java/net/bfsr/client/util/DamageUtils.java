package net.bfsr.client.util;

import clipper2.Clipper;
import clipper2.core.*;
import clipper2.engine.ClipperD;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import earcut4j.Earcut;
import net.bfsr.client.core.Core;
import net.bfsr.client.damage.Damagable;
import net.bfsr.client.entity.wreck.ShipWreckDamagable;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DamageUtils {
    private static final SweepLine SWEEP_LINE = new SweepLine();
    private static final Earcut EARCUT = new Earcut();
    public static final double SCALE = 10000.0;
    public static final double INV_SCALE = 1 / SCALE;
    private static final double MIN_AREA = 0.15;
    private static final ClipperOffset CLIPPER_OFFSET = new ClipperOffset(2.0);
    static Path64 path = new Path64();
    static Vector2 CACHED_VECTOR = new Vector2();
    static List<PathD> holes = new ArrayList<>(8);
    static Paths64 difference64 = new Paths64();
    static ClipperD clipper = new ClipperD();

    public static void damage(Damagable damagable, double contactX, double contactY, Path64 clip, float radius) {
        if (damagable.getContours().size() == 0) return;

        DamageMaskTexture maskTexture = damagable.getMaskTexture();
        Body body = damagable.getBody();
        double x = (float) body.getTransform().getTranslationX();
        double y = (float) body.getTransform().getTranslationY();
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();
        Vector2f scale = damagable.getScale();
        maskTexture.reset();
        clipTexture(contactX, contactY, damagable, radius, maskTexture);

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
                if (area > MIN_AREA) {
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
                            System.out.println("Error during decompose on client " + e.getMessage() + " Area: " + area);
                            for (int i = 0; i < vector2List.length; i++) {
                                System.out.println(vector2List[i].x + " " + vector2List[i].y);
                            }
                        }
                    }
                } else {
                    damagable.destroy();
                }

//                for (int i = 0; i < removedPaths.size(); i++) {
//                    PathD removedPath = removedPaths.get(i);
//                    area = Clipper.Area(removedPath);
//                    if (area > MIN_AREA) {
//                        DamageMaskTexture damageMaskTexture = createInvertedDamageTexture(removedPaths64.get(i), maskTexture, scale);
//                        ShipWreckDamagable damage = createDamage(0, (float) x, (float) y, (float) sin, (float) cos, scale.x, scale.y, removedPath, damageMaskTexture,
//                                TextureLoader.getTexture(TextureRegister.values()[maskTexture.getTextureIndex()]));
//                        if (damage != null) {
//                            damage.getBody().setLinearVelocity(damagable.getBody().getLinearVelocity());
//                            damage.getBody().setAngularVelocity(damagable.getBody().getAngularVelocity());
//                            Core.get().getWorld().addDamage(damage);
//                        }
//                    }
//                }

                if (!damagable.isDead()) {
                    clipTextureOutside(newHull64, maskTexture, scale);
                }
            } else if (difference64.size() > 0) {
                Path64 path64 = difference64.get(0);
                PathD path = new PathD(path64.size());
                for (int i1 = 0; i1 < path64.size(); i1++) {
                    Point64 point64 = path64.get(i1);
                    path.add(new PointD(point64, INV_SCALE));
                }

                double area = Clipper.Area(path);
                if (area > MIN_AREA) {
                    contours.add(path);
                    clipTextureOutside(path64, maskTexture, scale);
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
                                System.out.println("Error during decompose on client " + e.getMessage() + " Area: " + area);
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

        if (!damagable.isDead()) {
            maskTexture.upload();
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

    private static void fillTextureOutsidePolygon(PathD pathD, DamageMaskTexture maskTexture, byte value) {
        int size = pathD.size();
        int[] nodeX = new int[size];
        int nodes, i, j, swap, pixelX;
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        ByteBuffer byteBuffer = maskTexture.getByteBuffer();
        GL11.glPointSize(4);
        GL11.glBegin(GL11.GL_POINTS);

        for (int pixelY = 0; pixelY < maskTexture.getHeight(); pixelY++) {
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
                        int index = pixelY * maskTexture.getHeight() + pixelX;
                        byte currValue = byteBuffer.get(index);
                        if (currValue != value) {
                            GL11.glVertex2d(pixelX, pixelY);
                            byteBuffer.put(index, value);
                            onePixelPut = true;

                            if (pixelX < x) x = pixelX;
                            if (pixelX > maxX) maxX = pixelX;
                        }
                    }
                    startX = nodeX[i] + 1;
                    endX = i + 1 == nodes ? maskTexture.getWidth() : nodeX[i + 1];
                }

                for (pixelX = startX; pixelX < maskTexture.getWidth(); pixelX++) {
                    int index = pixelY * maskTexture.getHeight() + pixelX;
                    byte currValue = byteBuffer.get(index);
                    if (currValue != value) {
                        byteBuffer.put(index, value);
                        GL11.glVertex2d(pixelX, pixelY);
                        onePixelPut = true;

                        if (pixelX < x) x = pixelX;
                        if (pixelX > maxX) maxX = pixelX;
                    }
                }
            } else {
                for (pixelX = 0; pixelX < maskTexture.getWidth(); pixelX++) {
                    int index = pixelY * maskTexture.getHeight() + pixelX;
                    byte currValue = byteBuffer.get(index);
                    if (currValue != value) {
                        byteBuffer.put(index, value);
                        GL11.glVertex2d(pixelX, pixelY);
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

        GL11.glEnd();

        maskTexture.setX(Math.min(maskTexture.getX(), x));
        maskTexture.setY(Math.min(maskTexture.getY(), y));
        maskTexture.setMaxX(Math.max(maskTexture.getMaxX(), maxX));
        maskTexture.setMaxY(Math.max(maskTexture.getMaxY(), maxY));
    }

    private static void fillTextureByPolygon(PathD pathD, DamageMaskTexture maskTexture, byte value) {
        int size = pathD.size();
        int[] nodeX = new int[size];
        int nodes, swap, i, j, pixelX;
        ByteBuffer byteBuffer = maskTexture.getByteBuffer();

        for (int pixelY = 0; pixelY < maskTexture.getHeight(); pixelY++) {
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
                        byteBuffer.put(pixelY * maskTexture.getHeight() + pixelX, value);
                    }
                }
            }
        }

        GL45C.glTextureSubImage2D(maskTexture.getId(), 0, 0, 0, maskTexture.getWidth(), maskTexture.getHeight(), GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, byteBuffer);
    }

    public static void clipTextureOutside(Path64 path, DamageMaskTexture maskTexture, Vector2f scale) {
        clipTextureOutside(path, maskTexture, scale, 1.2 * SCALE);
    }

    public static PathD clipTextureOutside(Path64 path, DamageMaskTexture maskTexture, Vector2f scale, double offset) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = maskTexture.getWidth() / scale.x;
        float localScaleY = maskTexture.getHeight() / scale.y;

        CLIPPER_OFFSET.Clear();
        CLIPPER_OFFSET.AddPath(path, JoinType.Miter, EndType.Polygon);
        Paths64 paths = CLIPPER_OFFSET.Execute(offset / (localScaleX * 0.5f));
        try {
            Path64 path64 = paths.get(0);
//            GL11.glBegin(GL11.GL_LINE_LOOP);
            PathD res = new PathD(path.size());
            for (int i = 0, path64Size = path64.size(); i < path64Size; i++) {
                Point64 pt = path64.get(i);
//                GL11.glVertex2d((pt.x + sizeX) * localScaleX, (pt.y + sizeY) * localScaleY);
                res.add(new PointD((pt.x * INV_SCALE + sizeX) * localScaleX, (pt.y * INV_SCALE + sizeY) * localScaleY));
            }
//            GL11.glEnd();
            fillTextureOutsidePolygon(res, maskTexture, (byte) 0);
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

    public static void clipTexture(Path64 path, DamageMaskTexture damageMaskTexture, Vector2f scale) {
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        float localScaleX = damageMaskTexture.getWidth() / scale.x;
        float localScaleY = damageMaskTexture.getHeight() / scale.y;

        CLIPPER_OFFSET.Clear();
        CLIPPER_OFFSET.AddPath(path, JoinType.Miter, EndType.Polygon);
        Path64 result = CLIPPER_OFFSET.Execute(22.0 / localScaleX).get(0);

        int cnt = path.size();
        PathD res = new PathD(cnt);
        for (int i = 0, path64Size = result.size(); i < path64Size; i++) {
            Point64 pt = result.get(i);
            res.add(new PointD((pt.x * INV_SCALE + sizeX) * localScaleX, (pt.y * INV_SCALE + sizeY) * localScaleY));
        }

        fillTextureByPolygon(res, damageMaskTexture, (byte) 0);
    }

    public static void clipTexture(double x, double y, Damagable damagable, float clipRadius, DamageMaskTexture maskTexture) {
        Vector2f scale = damagable.getScale();
        DamageMaskTexture texture = damagable.getMaskTexture();
        double sin = -damagable.getBody().getTransform().getSint();
        double cos = damagable.getBody().getTransform().getCost();

        ByteBuffer byteBuffer = texture.getByteBuffer();
        float sizeX = scale.x / 2.0f;
        float sizeY = scale.y / 2.0f;
        int width = texture.getWidth();
        int height = texture.getHeight();
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

            for (int j = startY; j < maxY; j++) {
                for (int i = startX; i < maxX; i++) {
                    int dx = i - localX;
                    int dy = j - localY;
                    float square = (dx * dx + dy * dy) * (Core.get().getWorld().getRand().nextFloat(0.5f) + 0.5f);
                    int index = j * height + i;
                    if (square < radiusSq) {
                        float holeThreshold = radiusSq / 4.0f * Core.get().getWorld().getRand().nextFloat();
                        if (square <= holeThreshold) {
                            byteBuffer.put(index, value);
                        } else {
                            byteBuffer.put(index, (byte) Math.min((int) (((square - holeThreshold) / radiusSq) * 255), Byte.toUnsignedInt(byteBuffer.get(index))));
                        }
                    }
                }
            }

            maskTexture.setX(startX);
            maskTexture.setY(startY);
            maskTexture.setMaxX(maxX - 1);
            maskTexture.setMaxY(maxY - 1);
        }
    }

    public static DamageMaskTexture createInvertedDamageTexture(Path64 hull, DamageMaskTexture maskTexture, Vector2f scale) {
        ByteBuffer byteBuffer = maskTexture.getByteBuffer();
        ByteBuffer newByteBuffer = BufferUtils.createByteBuffer(byteBuffer.capacity());
        newByteBuffer.put(byteBuffer);
        newByteBuffer.flip();
        byteBuffer.position(0);

        DamageMaskTexture damagedTexture = new DamageMaskTexture(maskTexture.getWidth(), maskTexture.getHeight(), newByteBuffer);
        damagedTexture.createEmpty();
        PathD path = clipTextureOutside(hull, damagedTexture, scale, SCALE);
        for (int i = 0; i < path.size(); i++) {
            PointD point = path.get(i);
            if (point.x < damagedTexture.x) damagedTexture.x = (int) point.x;
            else if (point.x > damagedTexture.maxX) damagedTexture.maxX = (int) point.x;
            if (point.y < damagedTexture.y) damagedTexture.y = (int) point.y;
            else if (point.y > damagedTexture.maxY) damagedTexture.maxY = (int) point.y;
        }
        damagedTexture.upload();
        return damagedTexture;
    }

    public static ShipWreckDamagable createDamage(int id, float x, float y, float sin, float cos, float scaleX, float scaleY, PathD contour, DamageMaskTexture damageMaskTexture, Texture texture) {
        Vector2[] vectors = new Vector2[contour.size()];
        for (int i = 0; i < vectors.length; i++) {
            PointD pointD = contour.get(i);
            vectors[i] = new Vector2(pointD.x, pointD.y);
        }

        PathsD contours = new PathsD();
        contours.add(contour);

        if (vectors.length > 3) {
            try {
                return createDamage(id, x, y, sin, cos, scaleX, scaleY, SWEEP_LINE.decompose(vectors), contours, damageMaskTexture, texture);
            } catch (Exception e) {
//                System.out.println("Error during decompose on client " + e.getMessage());
//                for (int i = 0; i < vectors.length; i++) {
//                    System.out.println(vectors[i].x + " " + vectors[i].y);
//                }
                e.printStackTrace();
                return null;
            }
        } else {
            return createDamage(id, x, y, sin, cos, scaleX, scaleY, Collections.singletonList(Geometry.createPolygon(vectors)), contours, damageMaskTexture, texture);
        }
    }

    private static ShipWreckDamagable createDamage(int id, float x, float y, float sin, float cos, float scaleX, float scaleY, List<Convex> convexes, PathsD contours,
                                                   DamageMaskTexture maskTexture, Texture texture) {
        ShipWreckDamagable shipWreckDamagable = new ShipWreckDamagable(id, x, y, sin, cos, scaleX, scaleY, texture, maskTexture, contours);
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

    public static void decompose(PathsD contours, Consumer<Convex> consumer, SweepLine sweepLine, Earcut earcut) {
        if (contours.size() > 1) {
            earcut.execute(contours, consumer::accept);
        } else {
            PathD pathD = contours.get(0);
            Vector2[] vectors = new Vector2[pathD.size()];
            for (int i = 0; i < vectors.length; i++) {
                PointD pointD = pathD.get(i);
                vectors[i] = new Vector2(pointD.x, pointD.y);
            }

            if (vectors.length > 3) {
                try {
                    List<Convex> convexes = sweepLine.decompose(vectors);
                    for (int i = 0; i < convexes.size(); i++) {
                        consumer.accept(convexes.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println("Error during decompose on client " + e.getMessage());
//                    for (int i = 0; i < vectors.length; i++) {
//                        System.out.println(vectors[i].x + " " + vectors[i].y);
//                    }
                }
            } else {
                consumer.accept(Geometry.createPolygon(vectors));
            }
        }
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