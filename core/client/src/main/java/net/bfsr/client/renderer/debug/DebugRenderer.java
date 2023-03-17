package net.bfsr.client.renderer.debug;

import clipper2.core.*;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import net.bfsr.client.damage.Damagable;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.renderer.RenderUtils;
import net.bfsr.client.renderer.primitive.DynamicVertexColorBuffer;
import net.bfsr.client.renderer.primitive.VertexColorBuffer;
import net.bfsr.client.util.DamageUtils;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class DebugRenderer {
    public static final DebugRenderer INSTANCE = new DebugRenderer();

    private final VertexColorBuffer vertexColorBuffer = new VertexColorBuffer(4);
    private final DynamicVertexColorBuffer dynamicVertexColorBuffer = new DynamicVertexColorBuffer(4);
    private final Matrix4f modelMatrix = new Matrix4f();

    public void renderAABB(AABB aabb) {
        vertexColorBuffer.addVertex((float) aabb.getMinX(), (float) aabb.getMinY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMinX(), (float) aabb.getMaxY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMaxX(), (float) aabb.getMaxY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMaxX(), (float) aabb.getMinY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.flip();
        RenderUtils.render(GL11.GL_LINE_LOOP, vertexColorBuffer);
    }

    public void renderWound(Wound wound) {
        int length = wound.getVertices().length;
        dynamicVertexColorBuffer.reset();
        for (int i = 0; i < length; i++) {
            Vector2 vector = wound.getVertices()[i];
            dynamicVertexColorBuffer.addVertex((float) vector.x, (float) vector.y, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        dynamicVertexColorBuffer.flip();
        RenderUtils.render(GL11.GL_LINE_LOOP, dynamicVertexColorBuffer);
    }

    public void render(CollisionObject collisionObject) {
        Vector2f position = collisionObject.getPosition();

        AABB aabb = collisionObject.getAabb();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.1f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(aabb.getMinX(), aabb.getMinY());
        GL11.glVertex2d(aabb.getMinX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMinY());
        GL11.glEnd();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        render(collisionObject.getBody(), position.x, position.y);
        if (collisionObject instanceof Damagable damagable) {
            double rot = Math.toDegrees(collisionObject.getBody().getTransform().getRotationAngle());
            PathsD contours = damagable.getContours();
            if (contours != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef(position.x, position.y, 0.0f);
                GL11.glRotated(rot, 0, 0, 1);
                GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
                for (int i = 0; i < contours.size(); i++) {
                    PathD pathD = contours.get(i);
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    for (int i1 = 0; i1 < pathD.size(); i1++) {
                        PointD pointD = pathD.get(i1);
                        GL11.glVertex2d(pointD.x, pointD.y);
                    }
                    GL11.glEnd();

                    if (i == 0) {
                        ClipperOffset clipperOffset = new ClipperOffset();
                        Path64 path64 = new Path64(pathD.size());
                        for (int i1 = 0; i1 < pathD.size(); i1++) {
                            path64.add(new Point64(pathD.get(i1), DamageUtils.SCALE));
                        }
                        clipperOffset.AddPath(path64, JoinType.Miter, EndType.Polygon);
                        float localScaleX = damagable.getMaskTexture().getWidth() / damagable.getScale().x;
                        Path64 solution = clipperOffset.Execute(1.2f * DamageUtils.SCALE / (localScaleX * 0.5f)).get(0);
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        for (int i2 = 0, path64Size = solution.size(); i2 < path64Size; i2++) {
                            Point64 pointD = solution.get(i2);
                            GL11.glVertex2d(pointD.x * DamageUtils.INV_SCALE, pointD.y * DamageUtils.INV_SCALE);
                        }
                        GL11.glEnd();
                    }
                }
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glPopMatrix();
            }
        }
    }

    public void render(Body body, float x, float y) {
        double rot = Math.toDegrees(body.getTransform().getRotationAngle());

        GL11.glBegin(GL11.GL_LINE_LOOP);
        float velocityX = (float) body.getLinearVelocity().x;
        float velocityY = (float) body.getLinearVelocity().y;
        GL11.glVertex2d(body.getWorldCenter().x, body.getWorldCenter().y);
        GL11.glVertex2d(velocityX / 5.0f + body.getWorldCenter().x, velocityY / 5.0f + body.getWorldCenter().y);
        GL11.glEnd();

        List<BodyFixture> fixtures = body.getFixtures();
        for (int j = 0, fixturesSize = fixtures.size(); j < fixturesSize; j++) {
            BodyFixture bodyFixture = fixtures.get(j);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Rectangle rect) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0);
                GL11.glRotated(rot, 0, 0, 1);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                for (int i = 0; i < rect.getVertices().length; i++) {
                    Vector2 vertex = rect.getVertices()[i];
                    GL11.glVertex2d(vertex.x, vertex.y);
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            } else if (convex instanceof Polygon polygon) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0);
                GL11.glRotated(rot, 0, 0, 1);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                for (int i = 0; i < polygon.getVertices().length; i++) {
                    Vector2 vertex = polygon.getVertices()[i];
                    GL11.glVertex2d(vertex.x, vertex.y);
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            } else if (convex instanceof Circle circle) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                float count = 10.0f;
                float angleAdd = MathUtils.TWO_PI / count;
                float startAngle = 0.0f;
                for (int i = 0; i < count; i++) {
                    Vector2f pos = RotationHelper.angleToVelocity(startAngle, (float) circle.getRadius());
                    GL11.glVertex2f(pos.x, pos.y);
                    startAngle += angleAdd;
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            }
        }
    }
}