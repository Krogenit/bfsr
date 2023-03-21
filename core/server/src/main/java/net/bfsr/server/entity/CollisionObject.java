package net.bfsr.server.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.entity.GameObject;
import net.bfsr.math.LUT;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
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
    protected float lifeTime;
    @Getter
    protected float sin, cos;
    protected final Transform savedTransform = new Transform();

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

    public void updateServerPositionFromPacket(Vector2f pos, float angle, Vector2f velocity, float angularVelocity) {
        lifeTime = 0;
        body.setAtRest(false);
        setPosition(pos.x, pos.y);
        setRotation(angle);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.setAngularVelocity(angularVelocity);
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