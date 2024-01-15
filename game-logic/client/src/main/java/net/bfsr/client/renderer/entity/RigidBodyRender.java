package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.RigidBody;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

public class RigidBodyRender<T extends RigidBody<? extends GameObjectConfigData>> extends Render<T> {
    private static final org.dyn4j.geometry.AABB DYN4J_AABB = new org.dyn4j.geometry.AABB(0, 0, 0, 0);
    private static final net.bfsr.engine.util.AABB AABB = new net.bfsr.engine.util.AABB();

    private static final Vector4f CONVEX_COLOR = new Vector4f(0.6f, 0.8f, 1.0f, 0.75f);
    private static final Vector4f VELOCITY_COLOR = new Vector4f(0.6f, 1.0f, 0.8f, 0.25f);
    private static final Vector4f BODY_AABB_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.1f);
    private static final Vector4f RENDER_AABB_COLOR = new Vector4f(0.5f, 1.0f, 0.5f, 0.1f);

    private final AABB geometryAABB = new AABB(0, 0, 0, 0);
    private final Vector2f angleToVelocity = new Vector2f();

    RigidBodyRender(AbstractTexture texture, T object, float r, float g, float b, float a) {
        super(texture, object, r, g, b, a);
    }

    RigidBodyRender(AbstractTexture texture, T object) {
        this(texture, object, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public RigidBodyRender(T object) {
        this(Engine.assetsManager.getTexture(((GameObjectConfigData) ConfigConverterManager.INSTANCE
                        .getConverter(object.getRegistryId()).get(object.getDataId())).getTexture()), object,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastSin = object.getSin();
        lastCos = object.getCos();
    }

    @Override
    protected void updateAABB() {
        MathUtils.computeAABB(geometryAABB, object.getBody(), object.getBody().getTransform());
        aabb.set(geometryAABB.getMinX(), geometryAABB.getMinY(), geometryAABB.getMaxX(), geometryAABB.getMaxY());
    }

    @Override
    public void renderAlpha() {
        float sin = object.getSin();
        float cos = object.getCos();
        Vector2f position = object.getPosition();
        Vector2f scale = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, scale.x, scale.y, 1.0f, 1.0f, 1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderDebug() {
        Vector2f position = object.getPosition();
        Vector2f interpolatedPosition = new Vector2f(lastPosition.x + (position.x - lastPosition.x) * renderer.getInterpolation(),
                lastPosition.y + (position.y - lastPosition.y) * renderer.getInterpolation());

        Body body = object.getBody();
        body.computeAABB(DYN4J_AABB);
        debugRenderer.renderAABB(AABB.set(DYN4J_AABB.getMinX(), DYN4J_AABB.getMinY(), DYN4J_AABB.getMaxX(), DYN4J_AABB.getMaxY()),
                BODY_AABB_COLOR);

        debugRenderer.renderAABB(aabb, RENDER_AABB_COLOR);

        renderDebug(body, interpolatedPosition.x, interpolatedPosition.y,
                lastSin + (object.getSin() - lastSin) * renderer.getInterpolation(),
                lastCos + (object.getCos() - lastCos) * renderer.getInterpolation());
    }

    private void renderDebug(Body body, float x, float y, float sin, float cos) {
        float velocityX = (float) body.getLinearVelocity().x * 0.05f;
        float velocityY = (float) body.getLinearVelocity().y * 0.05f;
        debugRenderer.addCommand(2);
        Vector2 worldCenter = body.getWorldCenter();
        float worldCenterX = (float) worldCenter.x;
        float worldCenterY = (float) worldCenter.y;
        debugRenderer.addVertex(worldCenterX, worldCenterY, VELOCITY_COLOR);
        debugRenderer.addVertex(velocityX + worldCenterX, velocityY + worldCenterY, VELOCITY_COLOR);

        List<BodyFixture> fixtures = body.getFixtures();
        for (int j = 0, fixturesSize = fixtures.size(); j < fixturesSize; j++) {
            BodyFixture bodyFixture = fixtures.get(j);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Rectangle rect) {
                Vector2[] vertices = rect.getVertices();
                debugRenderer.addCommand(vertices.length);
                for (int i = 0; i < vertices.length; i++) {
                    Vector2 vertex = vertices[i];
                    debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, (float) vertex.x, (float) vertex.y),
                            y + RotationHelper.rotateY(sin, cos, (float) vertex.x, (float) vertex.y), CONVEX_COLOR);
                }
            } else if (convex instanceof Polygon polygon) {
                Vector2[] vertices = polygon.getVertices();
                debugRenderer.addCommand(vertices.length);
                for (int i = 0; i < vertices.length; i++) {
                    Vector2 vertex = vertices[i];
                    debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, (float) vertex.x, (float) vertex.y),
                            y + RotationHelper.rotateY(sin, cos, (float) vertex.x, (float) vertex.y), CONVEX_COLOR);
                }
            } else if (convex instanceof Circle circle) {
                int count = 10;
                float angleAdd = MathUtils.TWO_PI / count;
                float startAngle = 0.0f;
                debugRenderer.addCommand(count);
                for (int i = 0; i < count; i++) {
                    RotationHelper.angleToVelocity(startAngle, (float) circle.getRadius(), angleToVelocity);
                    debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, angleToVelocity.x, angleToVelocity.y),
                            y + RotationHelper.rotateY(sin, cos, angleToVelocity.x, angleToVelocity.y), CONVEX_COLOR);
                    startAngle += angleAdd;
                }
            }
        }
    }
}