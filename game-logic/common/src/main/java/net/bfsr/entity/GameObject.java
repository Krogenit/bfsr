package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Vector2f;

@NoArgsConstructor
public class GameObject {
    private final Vector2f position = new Vector2f();
    private final Vector2f size = new Vector2f();
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

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    protected void addPosition(float x, float y) {
        position.add(x, y);
    }

    public void setSize(float x, float y) {
        size.set(x, y);
    }

    public void addSize(float x, float y) {
        size.add(x, y);
    }

    public void setDead() {
        isDead = true;
    }

    public int getId() {
        return -1;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getSizeX() {
        return size.x;
    }

    public float getSizeY() {
        return size.y;
    }
}