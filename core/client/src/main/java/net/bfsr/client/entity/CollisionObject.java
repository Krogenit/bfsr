package net.bfsr.client.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.world.WorldClient;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.List;

@NoArgsConstructor
public abstract class CollisionObject extends TextureObject {
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    private static final AABB CACHED_AABB_0 = new AABB(0, 0, 0, 0);
    private static final AABB CACHED_AABB_1 = new AABB(0, 0, 0, 0);

    @Getter
    protected WorldClient world;
    @Getter
    protected final Body body = new Body();
    @Getter
    protected boolean isDead;
    @Getter
    @Setter
    protected int id;
    protected Vector2f velocity = new Vector2f();
    protected AxisAlignedBoundingBox aabb;
    @Getter
    protected AxisAlignedBoundingBox worldAABB;
    protected float lifeTime;
    @Getter
    protected float lastSin, lastCos;
    @Getter
    protected float sin, cos;

    protected CollisionObject(WorldClient world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a,
                              Texture texture) {
        super(texture, x, y, rotation, scaleX, scaleY, r, g, b, a);
        this.world = world;
        this.id = id;
        createBody(x, y);
        createAABB();
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float rotation, float scaleX, float scaleY, Texture texture) {
        this(world, id, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, texture);
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY, float r, float g, float b, float a,
                              Texture texture) {
        this(world, id, x, y, 0, scaleX, scaleY, r, g, b, a, texture);
        this.sin = sin;
        this.cos = cos;
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture) {
        this(world, id, x, y, 0, scaleX, scaleY, r, g, b, a, texture);
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float scaleX, float scaleY, Texture texture) {
        this(world, id, x, y, 0, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, texture);
    }

    protected CollisionObject(WorldClient world) {
        this.world = world;
    }

    protected abstract void createBody(float x, float y);

    protected void createAABB() {
        AABB aabb = computeAABB();

        if (this.aabb != null) {
            this.aabb.set((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
        } else {
            this.aabb = new AxisAlignedBoundingBox((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
            this.worldAABB = new AxisAlignedBoundingBox(this.aabb);
        }
    }

    protected AABB computeAABB() {
        List<BodyFixture> fixtures = body.getFixtures();
        int size = fixtures.size();
        fixtures.get(0).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_0);
        for (int i = 1; i < size; i++) {
            fixtures.get(i).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_1);
            CACHED_AABB_0.union(CACHED_AABB_1);
        }

        return CACHED_AABB_0;
    }

    protected void updateWorldAABB() {
        Vector2f position = getPosition();
        worldAABB.set(aabb.getMin().x + position.x, aabb.getMin().y + position.y, aabb.getMax().x + position.x, aabb.getMax().y + position.y);
    }

    @Override
    public void update() {
        lastSin = sin;
        lastCos = cos;
        lifeTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (lifeTime > 120) {
            setDead();
            lifeTime = 0;
        }
    }

    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        updateWorldAABB();
    }

    public void updateClientPositionFromPacket(Vector2f position, float rotation, Vector2f velocity, float angularVelocity) {
        lifeTime = 0;
        body.setAtRest(false);
        CollisionObjectUtils.updatePos(this, position);
        CollisionObjectUtils.updateRot(body, rotation);
        body.setLinearVelocity(velocity.x, velocity.y);
        updateAngularVelocity(angularVelocity);
    }

    private void updateAngularVelocity(float re) {
        body.setAngularVelocity(re);
    }

    @Override
    public float getRotation() {
        return (float) body.getTransform().getRotationAngle();
    }

    @Override
    public Vector2f getPosition() {
        Vector2 pos = body.getTransform().getTranslation();
        position.x = (float) pos.x;
        position.y = (float) pos.y;
        return position;
    }

    public Vector2f getVelocity() {
        Vector2 vel = body.getLinearVelocity();
        velocity.x = (float) vel.x;
        velocity.y = (float) vel.y;
        return velocity;
    }

    public float getAngularVelocity() {
        return (float) body.getAngularVelocity();
    }

    @Override
    public void setDead() {
        isDead = true;
    }
}
