package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

@Getter
@NoArgsConstructor
public class GameObject {
    protected final Vector2f position = new Vector2f();
    protected final Vector2f scale = new Vector2f();
    @Setter
    protected float rotation;

    public GameObject(float x, float y, float rotation, float scaleX, float scaleY) {
        this.position.set(x, y);
        this.rotation = rotation;
        this.scale.set(scaleX, scaleY);
    }

    public GameObject(float x, float y, float scaleX, float scaleY) {
        this(x, y, 0.0f, scaleX, scaleY);
    }

    public GameObject(float x, float y) {
        this(x, y, 0.0f, 0.0f, 0.0f);
    }

    public void update() {}

    public void postPhysicsUpdate() {}

    public void updateClientPositionFromPacket(Vector2f position, float angle, Vector2f velocity, float angularVelocity) {
        this.position.set(position);
    }

    public void collision(Body body, float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {}

    public void saveTransform(Transform transform) {}

    public void restoreTransform() {}

    public boolean canCollideWith(GameObject gameObject) {
        return this != gameObject;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void setScale(float x, float y) {
        this.scale.set(x, y);
    }

    public Body getBody() {
        return null;
    }

    public float getSin() {
        return 0.0f;
    }

    public float getCos() {
        return 0.0f;
    }

    public void setDead() {}

    public boolean isDead() {
        return false;
    }

    public int getId() {
        return 0;
    }

    public void clear() {}
}