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
    @Getter
    protected final Vector4f color = new Vector4f(1.0f);
    protected final Vector4f outlineColor = new Vector4f(1.0f);
    protected final Vector4f hoverColor = new Vector4f(1.0f);
    protected final Vector4f outlineHoverColor = new Vector4f(1.0f);
    @Setter
    private Supplier<Boolean> intersectsCheckMethod = () -> isIntersects(Mouse.getPosition().x, Mouse.getPosition().y);

    protected SimpleGuiObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected SimpleGuiObject(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
        updateLastPosition();

        if (!mouseWasHover && mouseHover) {
            onMouseHover();
        }
    }

    protected void updateLastPosition() {
        lastX = x;
        lastY = y;
    }

    @Override
    public void updateMouseHover() {
        boolean mouseHover = isIntersectsWithMouse();
        setMouseHover(mouseHover);
        if (mouseWasHover && !mouseHover) {
            onMouseStopHover();
        }
    }

    @Override
    public void render() {
        if (rotation != 0.0f) {
            GUIRenderer.get().add(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w);
            if (isMouseHover()) {
                GUIRenderer.get().add(lastX + 1, lastY + 1, x + 1, y + 1, lastRotation, rotation, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
            } else {
                GUIRenderer.get().add(lastX + 1, lastY + 1, x + 1, y + 1, lastRotation, rotation, width - 2, height - 2, color.x, color.y, color.z, color.w);
            }
        } else {
            if (isMouseHover()) {
                GUIRenderer.get().add(lastX, lastY, x, y, width, height, outlineHoverColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
                GUIRenderer.get().add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
            } else {
                GUIRenderer.get().add(lastX, lastY, x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
                GUIRenderer.get().add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
            }
        }
    }

    public void renderNoInterpolation() {
        if (isMouseHover()) {
            GUIRenderer.get().add(x, y, width, height, outlineHoverColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
            GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        } else {
            GUIRenderer.get().add(x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
            GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
        }
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

    @Override
    public SimpleGuiObject setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        return this;
    }

    public SimpleGuiObject setColor(Vector4f color) {
        this.color.set(color);
        return this;
    }

    @Override
    public SimpleGuiObject setOutlineColor(float r, float g, float b, float a) {
        outlineColor.set(r, g, b, a);
        return this;
    }

    public SimpleGuiObject setOutlineColor(Vector4f color) {
        outlineColor.set(color);
        return this;
    }

    @Override
    public SimpleGuiObject setOutlineHoverColor(float r, float g, float b, float a) {
        outlineHoverColor.set(r, g, b, a);
        return this;
    }

    public SimpleGuiObject setOutlineHoverColor(Vector4f color) {
        outlineHoverColor.set(color);
        return this;
    }

    @Override
    public SimpleGuiObject setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
        return this;
    }

    public SimpleGuiObject setHoverColor(Vector4f color) {
        hoverColor.set(color);
        return this;
    }

    @Override
    public SimpleGuiObject setTextColor(float r, float g, float b, float a) {
        return this;
    }

    public boolean isIntersectsWithMouse() {
        return intersectsCheckMethod.get();
    }

    public boolean isIntersects(float x, float y) {
        return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
    }

    public boolean isIntersects(Vector2f vector) {
        return isIntersects(vector.x, vector.y);
    }
}