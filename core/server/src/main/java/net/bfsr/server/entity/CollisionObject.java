package net.bfsr.server.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.entity.GameObject;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.CollisionObjectUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

@NoArgsConstructor
public class CollisionObject extends GameObject {
    @Getter
    protected WorldServer world;
    @Getter
    protected final Body body = new Body();
    @Getter
    @Setter
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
    protected float sin, cos;

    protected CollisionObject(WorldServer world, int id, float x, float y, float rotation, float scaleX, float scaleY) {
        super(x, y, rotation, scaleX, scaleY);
        this.world = world;
        this.id = id;
        this.body.getTransform().setTranslation(x, y);
    }

    protected CollisionObject(WorldServer world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY) {
        this(world, id, x, y, 0, scaleX, scaleY);
        this.sin = sin;
        this.cos = cos;
        this.body.getTransform().setRotation(sin, cos);
    }

    protected CollisionObject(WorldServer world, int id, float x, float y, float scaleX, float scaleY) {
        this(world, id, x, y, 0, scaleX, scaleY);
    }

    protected CollisionObject(WorldServer world) {
        this.world = world;
    }

    public void init() {
        initBody();
    }

    protected void initBody() {}

    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
    }

    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        lifeTime = 0;
        body.setAtRest(false);
        body.getTransform().setTranslation(pos.x, pos.y);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rot);
        body.setAngularVelocity(angularVelocity);
    }

    private void updateVelocity(Vector2f velocity) {
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    private void updatePos(Vector2f newPos) {
        Vector2f pos = getPosition();

        float dist = pos.distance(newPos);

        float alpha = dist > 10 ? 1.0f : 0.05f;
        double x = pos.x + alpha * (newPos.x - pos.x);
        double y = pos.y + alpha * (newPos.y - pos.y);

        body.getTransform().setTranslation(x, y);
    }

    private void updateRot(float re) {
        double rs = body.getTransform().getRotationAngle();

        double diff = re - rs;
        if (diff < -Math.PI) diff += Geometry.TWO_PI;
        if (diff > Math.PI) diff -= Geometry.TWO_PI;

        float alpha = (float) (Math.max(Math.abs(diff) / 10.0f, 0.1f));

        double a = diff * alpha + rs;

        body.getTransform().setRotation(a);
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
}
