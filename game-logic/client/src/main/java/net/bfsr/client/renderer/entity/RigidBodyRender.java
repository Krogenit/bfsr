package net.bfsr.client.renderer.entity;

import clipper2.core.*;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import net.bfsr.client.renderer.Render;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.Damageable;
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

    private static final Vector4f CONTOUR_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 1.0f);
    private static final Vector4f CONTOUR_OFFSET_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 0.6f);
    private static final Vector4f CONVEX_COLOR = new Vector4f(0.6f, 0.8f, 1.0f, 0.75f);
    private static final Vector4f VELOCITY_COLOR = new Vector4f(0.6f, 1.0f, 0.8f, 0.25f);
    public static final Vector4f BODY_AABB_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.1f);
    public static final Vector4f RENDER_AABB_COLOR = new Vector4f(0.5f, 1.0f, 0.5f, 0.1f);

    private final AABB geometryAABB = new AABB(0, 0, 0, 0);

    public RigidBodyRender(AbstractTexture texture, T object, float r, float g, float b, float a) {
        super(texture, object, r, g, b, a);
    }

    public RigidBodyRender(AbstractTexture texture, T object) {
        super(texture, object);
    }

    public RigidBodyRender(T rigidBody) {
        super(Engine.assetsManager.getTexture(
                ((GameObjectConfigData) ConfigConverterManager.INSTANCE.getConverter(rigidBody.getRegistryId())
                        .get(rigidBody.getDataId())).getTexture()), rigidBody);
    }

    @Override
    public void update() {
        Body body = object.getBody();
        lastPosition.x = (float) body.getTransform().getTranslationX();
        lastPosition.y = (float) body.getTransform().getTranslationY();
        lastSin = (float) body.getTransform().getSint();
        lastCos = (float) body.getTransform().getCost();
    }

    @Override
    protected void updateAABB() {
        object.getBody().computeAABB(geometryAABB);
        aabb.set(geometryAABB.getMinX(), geometryAABB.getMinY(), geometryAABB.getMaxX(),
                geometryAABB.getMaxY());
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

        Body body = object.getBody();
        body.computeAABB(DYN4J_AABB);
        debugRenderer.renderAABB(AABB.set(DYN4J_AABB.getMinX(), DYN4J_AABB.getMinY(), DYN4J_AABB.getMaxX(), DYN4J_AABB.getMaxY()),
                BODY_AABB_COLOR);

        debugRenderer.renderAABB(aabb, RENDER_AABB_COLOR);

        float sin = (float) body.getTransform().getSint();
        float cos = (float) body.getTransform().getCost();
        renderDebug(body, position.x, position.y, sin, cos);
        if (object instanceof Damageable<?> damageable) {
            PathsD contours = damageable.getContours();
            if (contours != null) {
                for (int i = 0; i < contours.size(); i++) {
                    PathD pathD = contours.get(i);
                    debugRenderer.addCommand(pathD.size());
                    for (int i1 = 0; i1 < pathD.size(); i1++) {
                        PointD pointD = pathD.get(i1);
                        debugRenderer.addVertex(
                                position.x + RotationHelper.rotateX(sin, cos, (float) pointD.x, (float) pointD.y),
                                position.y + RotationHelper.rotateY(sin, cos, (float) pointD.x, (float) pointD.y), CONTOUR_COLOR
                        );
                    }

                    if (i == 0) {
                        ClipperOffset clipperOffset = new ClipperOffset();
                        Path64 path64 = new Path64(pathD.size());
                        for (int i1 = 0; i1 < pathD.size(); i1++) {
                            path64.add(new Point64(pathD.get(i1), DamageSystem.SCALE));
                        }

                        clipperOffset.AddPath(path64, JoinType.Miter, EndType.Polygon);
                        Path64 solution = clipperOffset.Execute(0.25f * DamageSystem.SCALE).get(0);
                        debugRenderer.addCommand(solution.size());
                        for (int i2 = 0, path64Size = solution.size(); i2 < path64Size; i2++) {
                            Point64 pointD = solution.get(i2);
                            float x = (float) (pointD.x * DamageSystem.INV_SCALE);
                            float y = (float) (pointD.y * DamageSystem.INV_SCALE);
                            debugRenderer.addVertex(position.x + RotationHelper.rotateX(sin, cos, x, y),
                                    position.y + RotationHelper.rotateY(sin, cos, x, y), CONTOUR_OFFSET_COLOR);
                        }
                    }
                }
            }
        }
    }

    private void renderDebug(Body body, float x, float y, float sin, float cos) {
        float velocityX = (float) body.getLinearVelocity().x * 0.05f;
        float velocityY = (float) body.getLinearVelocity().y * 0.05f;
        debugRenderer.addCommand(2);
        Vector2 worldCenter = body.getWorldCenter();
        float worldCenterX = (float) worldCenter.x;
        float worldCenterY = (float) worldCenter.y;
        debugRenderer.addVertex(worldCenterX, worldCenterY, VELOCITY_COLOR);
        debugRenderer.addVertex(velocityX + worldCenterX, velocityY + worldCenterY,
                VELOCITY_COLOR);

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
                    Vector2f pos = RotationHelper.angleToVelocity(startAngle, (float) circle.getRadius());
                    debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, pos.x, pos.y),
                            y + RotationHelper.rotateY(sin, cos, pos.x, pos.y), CONVEX_COLOR);
                    startAngle += angleAdd;
                }
            }
        }
    }
}