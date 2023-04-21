package net.bfsr.client.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.world.WorldClient;
import net.bfsr.math.LUT;
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
public class CollisionObject extends TextureObject {
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
    @Getter
    protected AABB aabb = new AABB(0, 0, 0, 0);
    @Setter
    protected float lifeTime;
    @Getter
    protected float lastSin, lastCos;
    @Getter
    protected float sin, cos;
    protected final Transform savedTransform = new Transform();

    protected CollisionObject(WorldClient world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a,
                              Texture texture) {
        super(texture, x, y, rotation, scaleX, scaleY, r, g, b, a);
        this.world = world;
        this.id = id;
        this.body.getTransform().setTranslation(x, y);
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float rotation, float scaleX, float scaleY, Texture texture) {
        this(world, id, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, texture);
    }

    protected CollisionObject(WorldClient world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY, float r, float g, float b, float a,
                              Texture texture) {
        this(world, id, x, y, 0, scaleX, scaleY, r, g, b, a, texture);
        this.sin = sin;
        this.cos = cos;
        this.body.getTransform().setRotation(sin, cos);
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

    public void init() {
        initBody();
    }

    protected void initBody() {}

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

    protected void updateAABB() {
        body.computeAABB(aabb);
    }

    @Override
    public void update() {
        lastSin = sin;
        lastCos = cos;
        updateLifeTime();
    }

    protected void updateLifeTime() {
        lifeTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (lifeTime > 120) {
            setDead();
            lifeTime = 0;
        }
    }

    @Override
    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
        updateAABB();
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f position, float angle, Vector2f velocity, float angularVelocity) {
        lifeTime = 0;
        body.setAtRest(false);
        CollisionObjectUtils.updatePos(this, position);
        CollisionObjectUtils.updateRot(this, angle);
        body.setLinearVelocity(velocity.x, velocity.y);
        updateAngularVelocity(angularVelocity);
    }

    private void updateAngularVelocity(float re) {
        body.setAngularVelocity(re);
    }

    @Override
    public void saveTransform(Transform transform) {
        savedTransform.set(transform);
    }

    @Override
    public void restoreTransform() {
        body.setTransform(savedTransform);
    }

    @Override
    public void setDead() {
        isDead = true;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        body.getTransform().setTranslation(x, y);
    }

    public void setRotation(float sin, float cos) {
        this.sin = sin;
        this.cos = cos;
        body.getTransform().setRotation(sin, cos);
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        double sin = LUT.sin(rotation);
        double cos = LUT.cos(rotation);
        this.sin = (float) sin;
        this.cos = (float) cos;
        body.getTransform().setRotation(sin, cos);
    }

    @Override
    public Vector2f getPosition() {
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
        return position;
    }

    public float getX() {
        return (float) body.getTransform().getTranslationX();
    }

    public float getY() {
        return (float) body.getTransform().getTranslationY();
    }

    @Override
    public float getRotation() {
        return (float) body.getTransform().getRotationAngle();
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
}