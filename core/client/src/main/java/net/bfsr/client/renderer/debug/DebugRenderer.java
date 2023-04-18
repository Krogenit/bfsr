package net.bfsr.client.renderer.debug;

import clipper2.core.*;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import net.bfsr.client.core.Core;
import net.bfsr.client.damage.Damagable;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.renderer.primitive.VAO;
import net.bfsr.client.shader.DebugShader;
import net.bfsr.client.util.DamageUtils;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class DebugRenderer {
    private static final int VERTEX_DATA_SIZE = 6;
    private static final int VERTEX_DATA_SIZE_IN_BYTES = VERTEX_DATA_SIZE << 2;
    private static final int COMMAND_SIZE_IN_BYTES = 16;
    private static final Vector4f CONTOUR_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 1.0f);
    private static final Vector4f CONTOUR_OFFSET_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 0.6f);
    private static final Vector4f CONVEX_COLOR = new Vector4f(0.6f, 0.8f, 1.0f, 1.0f);
    private static final Vector4f AABB_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.1f);
    private static final Vector4f VELOCITY_COLOR = new Vector4f(0.6f, 1.0f, 0.8f, 0.5f);

    private final DebugShader debugShader = new DebugShader();
    private VAO vao;
    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(16);
    private ByteBuffer cmdBuffer = BufferUtils.createByteBuffer(16);
    private int objectsCount;

    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffers();
        vao.attributeBindingAndFormat(0, 2, 0, 0);
        vao.attributeBindingAndFormat(1, 4, 0, 8);
        vao.enableAttributes(2);
        debugShader.load();
        debugShader.init();
    }

    public void bind() {
        vao.bind();
        debugShader.enable();
    }

    private void renderAABB(AABB aabb) {
        addCommand(4);
        addVertex((float) aabb.getMinX(), (float) aabb.getMinY(), AABB_COLOR);
        addVertex((float) aabb.getMinX(), (float) aabb.getMaxY(), AABB_COLOR);
        addVertex((float) aabb.getMaxX(), (float) aabb.getMaxY(), AABB_COLOR);
        addVertex((float) aabb.getMaxX(), (float) aabb.getMinY(), AABB_COLOR);
    }

    public void render(CollisionObject collisionObject) {
        Vector2f position = collisionObject.getPosition();

        AABB aabb = collisionObject.getAabb();
        renderAABB(aabb);

        float sin = (float) collisionObject.getBody().getTransform().getSint();
        float cos = (float) collisionObject.getBody().getTransform().getCost();
        render(collisionObject.getBody(), position.x, position.y, sin, cos);
        if (collisionObject instanceof Damagable damagable) {
            PathsD contours = damagable.getContours();
            if (contours != null) {
                for (int i = 0; i < contours.size(); i++) {
                    PathD pathD = contours.get(i);
                    addCommand(pathD.size());
                    for (int i1 = 0; i1 < pathD.size(); i1++) {
                        PointD pointD = pathD.get(i1);
                        addVertex(position.x + RotationHelper.rotateX(sin, cos, (float) pointD.x, (float) pointD.y),
                                position.y + RotationHelper.rotateY(sin, cos, (float) pointD.x, (float) pointD.y), CONTOUR_COLOR);
                    }

                    if (i == 0) {
                        ClipperOffset clipperOffset = new ClipperOffset();
                        Path64 path64 = new Path64(pathD.size());
                        for (int i1 = 0; i1 < pathD.size(); i1++) {
                            path64.add(new Point64(pathD.get(i1), DamageUtils.SCALE));
                        }
                        clipperOffset.AddPath(path64, JoinType.Miter, EndType.Polygon);
                        float localScaleX = damagable.getMaskTexture().getWidth() / damagable.getScale().x;
                        Path64 solution = clipperOffset.Execute(1.2f * DamageUtils.SCALE / (localScaleX * 0.5f)).get(0);
                        addCommand(solution.size());
                        for (int i2 = 0, path64Size = solution.size(); i2 < path64Size; i2++) {
                            Point64 pointD = solution.get(i2);
                            float x = (float) (pointD.x * DamageUtils.INV_SCALE);
                            float y = (float) (pointD.y * DamageUtils.INV_SCALE);
                            addVertex(position.x + RotationHelper.rotateX(sin, cos, x, y), position.y + RotationHelper.rotateY(sin, cos, x, y), CONTOUR_OFFSET_COLOR);
                        }
                    }
                }
            }
        }
    }

    public void render(Body body, float x, float y, float sin, float cos) {
        float velocityX = (float) body.getLinearVelocity().x;
        float velocityY = (float) body.getLinearVelocity().y;
        addCommand(2);
        addVertex((float) body.getWorldCenter().x, (float) body.getWorldCenter().y, VELOCITY_COLOR);
        addVertex((float) (velocityX / 5.0f + body.getWorldCenter().x), (float) (velocityY / 5.0f + body.getWorldCenter().y), VELOCITY_COLOR);

        List<BodyFixture> fixtures = body.getFixtures();
        for (int j = 0, fixturesSize = fixtures.size(); j < fixturesSize; j++) {
            BodyFixture bodyFixture = fixtures.get(j);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Rectangle rect) {
                Vector2[] vertices = rect.getVertices();
                addCommand(vertices.length);
                for (int i = 0; i < vertices.length; i++) {
                    Vector2 vertex = vertices[i];
                    addVertex(x + RotationHelper.rotateX(sin, cos, (float) vertex.x, (float) vertex.y), y + RotationHelper.rotateY(sin, cos, (float) vertex.x, (float) vertex.y),
                            CONVEX_COLOR);
                }
            } else if (convex instanceof Polygon polygon) {
                Vector2[] vertices = polygon.getVertices();
                addCommand(vertices.length);
                for (int i = 0; i < vertices.length; i++) {
                    Vector2 vertex = vertices[i];
                    addVertex(x + RotationHelper.rotateX(sin, cos, (float) vertex.x, (float) vertex.y), y + RotationHelper.rotateY(sin, cos, (float) vertex.x, (float) vertex.y),
                            CONVEX_COLOR);
                }
            } else if (convex instanceof Circle circle) {
                int count = 10;
                float angleAdd = MathUtils.TWO_PI / count;
                float startAngle = 0.0f;
                addCommand(count);
                for (int i = 0; i < count; i++) {
                    Vector2f pos = RotationHelper.angleToVelocity(startAngle, (float) circle.getRadius());
                    addVertex(x + RotationHelper.rotateX(sin, cos, pos.x, pos.y), y + RotationHelper.rotateY(sin, cos, pos.x, pos.y), CONVEX_COLOR);
                    startAngle += angleAdd;
                }
            }
        }
    }

    public void render(int type) {
        vao.updateVertexBuffer(0, vertexBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE_IN_BYTES);
        vao.updateBuffer(1, cmdBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT);
        vao.bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, 1);
        GL43.glMultiDrawArraysIndirect(type, 0, objectsCount, 0);
        Core.get().getRenderer().increaseDrawCalls();
    }

    private void addCommand(int count) {
        checkBuffersSize(count);
        cmdBuffer.putInt(count);
        cmdBuffer.putInt(1);//instance count
        cmdBuffer.putInt(vertexBuffer.position() / VERTEX_DATA_SIZE);//first vertex index
        cmdBuffer.putInt(objectsCount++);//base instance
    }

    private void addVertex(float x, float y, Vector4f color) {
        addVertex(x, y, color.x, color.y, color.z, color.w);
    }

    private void addVertex(float x, float y, float r, float g, float b, float a) {
        vertexBuffer.put(x).put(y).put(r).put(g).put(b).put(a);
    }

    void checkBuffersSize(int vertexCount) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < vertexCount * VERTEX_DATA_SIZE) {
            vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
        }

        while (cmdBuffer.capacity() - cmdBuffer.position() < COMMAND_SIZE_IN_BYTES) {
            cmdBuffer = BufferUtils.createByteBuffer(cmdBuffer.capacity() << 1);
        }
    }

    public void clear() {
        vertexBuffer.clear();
        cmdBuffer.clear();
        objectsCount = 0;
    }
}