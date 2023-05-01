package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

@Getter
@NoArgsConstructor
public class GameObject {
    protected final Vector2f position = new Vector2f();
    protected final Vector2f size = new Vector2f();
    @Getter
    protected boolean isDead;

    public GameObject(float x, float y, float sizeX, float sizeY) {
        this.position.set(x, y);
        this.size.set(sizeX, sizeY);
    }

    public GameObject(float sizeX, float sizeY) {
        this(0.0f, 0.0f, sizeX, sizeY);
    }

    public void update() {}

    public void postPhysicsUpdate() {}

    public void collision(Body body, float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {}

    public void saveTransform(Transform transform) {}

    public void restoreTransform() {}

    public boolean canCollideWith(GameObject gameObject) {
        return this != gameObject;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void setSize(float x, float y) {
        this.size.set(x, y);
    }

    public void setDead() {
        isDead = true;
    }

    public int getId() {
        return -1;
    }

    public void clear() {}
}