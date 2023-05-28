package net.bfsr.client.renderer;

import clipper2.core.*;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.util.AABB;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.List;

@Getter
@NoArgsConstructor
public class Render<T extends GameObject> {
    private static final org.dyn4j.geometry.AABB DYN4J_AABB = new org.dyn4j.geometry.AABB(0, 0, 0, 0);
    private static final AABB AABB = new AABB();

    private static final Vector4f CONTOUR_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 1.0f);
    private static final Vector4f CONTOUR_OFFSET_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 0.6f);
    private static final Vector4f CONVEX_COLOR = new Vector4f(0.6f, 0.8f, 1.0f, 0.75f);
    private static final Vector4f VELOCITY_COLOR = new Vector4f(0.6f, 1.0f, 0.8f, 0.25f);

    protected T object;
    @Setter
    protected AbstractTexture texture;
    protected final Vector2f lastSize = new Vector2f();
    protected final Vector2f lastPosition = new Vector2f();
    protected final Vector4f color = new Vector4f();
    protected final Vector4f lastColor = new Vector4f();
    @Getter
    protected float lastSin, lastCos;
    protected final AABB aabb = new AABB();
    protected final AbstractRenderer renderer = Engine.renderer;
    protected final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;
    protected final AbstractDebugRenderer debugRenderer = renderer.debugRenderer;

    public Render(AbstractTexture texture, T object, float r, float g, float b, float a) {
        this.object = object;
        this.texture = texture;
        this.lastPosition.set(object.getPosition());
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.lastSize.set(object.getSize());
    }

    public Render(AbstractTexture texture, T gameObject) {
        this(texture, gameObject, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Render(T gameObject) {
        this(AbstractTextureLoader.dummyTexture, gameObject);
    }

    public void update() {}

    public void postWorldUpdate() {
        updateAABB();
    }

    public void renderAlpha() {}

    public void renderAdditive() {}

    public void renderDebug() {
        if (object instanceof RigidBody rigidBody) {
            renderDebug(rigidBody);
        }
    }

    public void renderDebug(RigidBody rigidBody) {
        Vector2f position = rigidBody.getPosition();

        rigidBody.getBody().computeAABB(DYN4J_AABB);
        debugRenderer.renderAABB(
                AABB.set(DYN4J_AABB.getMinX(), DYN4J_AABB.getMinY(), DYN4J_AABB.getMaxX(), DYN4J_AABB.getMaxY())
        );

        float sin = (float) rigidBody.getBody().getTransform().getSint();
        float cos = (float) rigidBody.getBody().getTransform().getCost();
        renderDebug(rigidBody.getBody(), position.x, position.y, sin, cos);
        if (rigidBody instanceof Damageable damageable) {
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
                        Render<?> render = Core.get().getRenderManager().getRender(damageable.getId());
                        float localScaleX = render.getMaskTexture().getWidth() / damageable.getSize().x;
                        Path64 solution = clipperOffset.Execute(1.2f * DamageSystem.SCALE / (localScaleX * 0.5f)).get(0);
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

    public void renderDebug(Body body, float x, float y, float sin, float cos) {
        float velocityX = (float) body.getLinearVelocity().x * 0.05f;
        float velocityY = (float) body.getLinearVelocity().y * 0.05f;
        debugRenderer.addCommand(2);
        debugRenderer.addVertex((float) body.getWorldCenter().x, (float) body.getWorldCenter().y, VELOCITY_COLOR);
        debugRenderer.addVertex((float) (velocityX + body.getWorldCenter().x), (float) (velocityY + body.getWorldCenter().y),
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

    protected void updateAABB() {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float halfSizeX = size.x / 2;
        float halfSizeY = size.y / 2;
        aabb.set(position.x - halfSizeX, position.y - halfSizeY, position.x + halfSizeX, position.y + halfSizeY);
    }

    protected void updateAABB(float sin, float cos) {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float halfSizeX = size.x / 2;
        float halfSizeY = size.y / 2;
        RotationHelper.rotateAABB(sin, cos, -halfSizeX, -halfSizeY, halfSizeX, halfSizeY, position.x, position.y, aabb);
    }

    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {}

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public boolean isDead() {
        return object.isDead();
    }

    public DamageMaskTexture getMaskTexture() {
        return null;
    }

    public void clear() {}
}