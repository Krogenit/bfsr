package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.util.SyncUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

@NoArgsConstructor
public class RigidBody extends GameObject {
    @Getter
    protected World world;
    @Getter
    protected final Body body = new Body();
    @Getter
    @Setter
    protected int id;
    protected Vector2f velocity = new Vector2f();
    @Getter
    protected float lifeTime;
    @Getter
    protected float sin, cos;
    private final Transform savedTransform = new Transform();
    protected EventBus eventBus;

    protected RigidBody(float x, float y, float sin, float cos, float scaleX, float scaleY) {
        super(x, y, scaleX, scaleY);
        this.sin = sin;
        this.cos = cos;
        this.body.getTransform().setTranslation(x, y);
        this.body.getTransform().setRotation(sin, cos);
    }

    protected RigidBody(float x, float y, float scaleX, float scaleY) {
        this(x, y, 0.0f, 1.0f, scaleX, scaleY);
    }

    protected RigidBody(float scaleX, float scaleY) {
        this(0, 0, scaleX, scaleY);
    }

    public void init(World world, int id) {
        this.world = world;
        this.id = id;
        this.eventBus = world.getEventBus();
        initBody();
    }

    protected void initBody() {}

    @Override
    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
    }

    public void updateClientPositionFromPacket(Vector2f position, float sin, float cos, Vector2f velocity,
                                               float angularVelocity) {
        lifeTime = 0;
        body.setAtRest(false);
        SyncUtils.updatePos(this, position);
        SyncUtils.updateRot(this, sin, cos);
        body.setLinearVelocity(velocity.x, velocity.y);
        setAngularVelocity(angularVelocity);
    }

    public void updateServerPositionFromPacket(Vector2f pos, float sin, float cos, Vector2f velocity, float angularVelocity) {
        body.setAtRest(false);
        setPosition(pos.x, pos.y);
        setRotation(sin, cos);
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
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        body.getTransform().setTranslation(x, y);
    }

    public void setRotation(float sin, float cos) {
        this.sin = sin;
        this.cos = cos;
        body.getTransform().setRotation(sin, cos);
    }

    private void setAngularVelocity(float angularVelocity) {
        body.setAngularVelocity(angularVelocity);
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