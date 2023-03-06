package net.bfsr.client.renderer.debug;

import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.renderer.RenderUtils;
import net.bfsr.client.renderer.primitive.DynamicVertexColorBuffer;
import net.bfsr.client.renderer.primitive.VertexColorBuffer;
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
        render(collisionObject.getBody(), position.x, position.y);
    }

    public void render(Body body, float x, float y) {
        org.dyn4j.geometry.AABB aabb = body.createAABB();
        double rot = Math.toDegrees(body.getTransform().getRotationAngle());

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(aabb.getMinX(), aabb.getMinY());
        GL11.glVertex2d(aabb.getMinX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMinY());
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        float velocityX = (float) body.getLinearVelocity().x;
        float velocityY = (float) body.getLinearVelocity().y;
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(velocityX / 5.0f + x, velocityY / 5.0f + y);
        GL11.glEnd();

        List<BodyFixture> fixtures = body.getFixtures();
        for (BodyFixture bodyFixture : fixtures) {
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
