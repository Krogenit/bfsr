package net.bfsr.client.gui;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.Supplier;

@NoArgsConstructor
public class SimpleGuiObject extends AbstractGuiObject {
    @Getter
    protected int lastX, lastY;
    @Getter
    protected int x, y;
    @Getter
    protected int width, height;
    @Setter
    protected float lastRotation;
    @Getter
    @Setter
    protected float rotation;
    protected Vector4f color = new Vector4f(1.0f);
    @Setter
    private Supplier<Boolean> intersectsCheckMethod = () -> isIntersects(Mouse.getPosition().x, Mouse.getPosition().y);

    protected SimpleGuiObject(int x, int y, int width, int height) {
        setPositionAndSize(x, y, width, height);
    }

    @Override
    public void update() {
        updateLastPosition();
    }

    protected void updateLastPosition() {
        lastX = x;
        lastY = y;
    }

    @Override
    public void render() {
        if (rotation != 0.0f) {
            GUIRenderer.get().add(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w);
        } else {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w);
        }
    }

    public void renderNoInterpolation() {
        GUIRenderer.get().add(x, y, width, height, color.x, color.y, color.z, color.w);
    }

    public void setPositionAndSize(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public SimpleGuiObject setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public SimpleGuiObject setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public SimpleGuiObject setHeight(int height) {
        this.height = height;
        return this;
    }

    public SimpleGuiObject setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        return this;
    }

    public boolean isIntersects() {
        return intersectsCheckMethod.get();
    }

    public boolean isIntersects(float x, float y) {
        return x >= this.x && y >= this.y && x <= this.x + width && y <= this.y + height;
    }

    public boolean isIntersects(Vector2f vector) {
        return isIntersects(vector.x, vector.y);
    }
}