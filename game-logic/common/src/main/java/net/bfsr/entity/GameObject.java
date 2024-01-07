package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Vector2f;

@Getter
@NoArgsConstructor
public class GameObject {
    protected final Vector2f position = new Vector2f();
    protected final Vector2f size = new Vector2f();
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

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setSize(float x, float y) {
        size.set(x, y);
    }

    public void setDead() {
        isDead = true;
    }

    public int getId() {
        return -1;
    }
}