package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.RigidBody;
import net.bfsr.math.RotationHelper;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector4f;

import java.nio.file.Path;

public class RigidBodyRender extends Render {
    private static final AABB CACHE = new AABB();
    private static final AABB AABB = new AABB();

    private static final Vector4f CONVEX_COLOR = new Vector4f(0.6f, 0.8f, 1.0f, 0.75f);
    private static final Vector4f VELOCITY_COLOR = new Vector4f(0.6f, 1.0f, 0.8f, 0.25f);
    private static final Vector4f BODY_AABB_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.1f);
    private static final Vector4f RENDER_AABB_COLOR = new Vector4f(0.5f, 1.0f, 0.5f, 0.1f);

    private final AABB geometryAABB = new AABB();
    private final AABB cache = new AABB();

    protected final RigidBody rigidBody;

    RigidBodyRender(AbstractTexture texture, RigidBody rigidBody, float r, float g, float b, float a) {
        super(texture, rigidBody, r, g, b, a);
        this.rigidBody = rigidBody;
    }

    RigidBodyRender(AbstractTexture texture, RigidBody rigidBody) {
        this(texture, rigidBody, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public RigidBodyRender(RigidBody rigidBody, Path texturePath) {
        this(Engine.assetsManager.getTexture(texturePath), rigidBody,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void update() {
        lastPosition.set(rigidBody.getX(), rigidBody.getY());
        lastSin = rigidBody.getSin();
        lastCos = rigidBody.getCos();
    }

    @Override
    protected void updateAABB() {
        MathUtils.computeAABB(geometryAABB, rigidBody.getBody(), rigidBody.getBody().getTransform(), cache);
        aabb.set(geometryAABB.getMinX(), geometryAABB.getMinY(), geometryAABB.getMaxX(), geometryAABB.getMaxY());
    }

    @Override
    public void renderAlpha() {
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, rigidBody.getX(), rigidBody.getY(), lastSin, lastCos, sin,
                cos, rigidBody.getSizeX(), rigidBody.getSizeY(), 1.0f, 1.0f, 1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderDebug() {
        Body body = rigidBody.getBody();
        MathUtils.computeAABB(AABB, body, body.getTransform(), CACHE);
        debugRenderer.renderAABB(AABB, BODY_AABB_COLOR);
        debugRenderer.renderAABB(aabb, RENDER_AABB_COLOR);

        renderDebug(body, lastPosition.x + (object.getX() - lastPosition.x) * renderer.getInterpolation(),
                lastPosition.y + (object.getY() - lastPosition.y) * renderer.getInterpolation(),
                lastSin + (rigidBody.getSin() - lastSin) * renderer.getInterpolation(),
                lastCos + (rigidBody.getCos() - lastCos) * renderer.getInterpolation());
    }

    private void renderDebug(Body body, float x, float y, float sin, float cos) {
        Vector2 linearVelocity = body.getLinearVelocity();
        float velocityX = linearVelocity.x * 0.05f;
        float velocityY = linearVelocity.y * 0.05f;
        debugRenderer.addCommand(2);
        Vector2 worldCenter = body.getWorldCenter();
        debugRenderer.addVertex(worldCenter.x, worldCenter.y, VELOCITY_COLOR);
        debugRenderer.addVertex(velocityX + worldCenter.x, velocityY + worldCenter.y, VELOCITY_COLOR);

        for (int i = 0; i < body.fixtures.size(); i++) {
            Fixture fixture = body.fixtures.get(i);
            Polygon polygon = ((Polygon) fixture.getShape());
            Vector2[] vertices = polygon.getVertices();
            debugRenderer.addCommand(vertices.length);
            for (int j = 0; j < polygon.count; j++) {
                Vector2 vertex = vertices[j];
                debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, vertex.x, vertex.y),
                        y + RotationHelper.rotateY(sin, cos, vertex.x, vertex.y), CONVEX_COLOR);
            }
        }
    }
}