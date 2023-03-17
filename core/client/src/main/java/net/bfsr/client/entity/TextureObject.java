package net.bfsr.client.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Getter
public class TextureObject extends GameObject {
    protected Texture texture;
    protected final Vector2f lastScale = new Vector2f();
    protected final Vector2f lastPosition = new Vector2f();
    protected final Vector4f color = new Vector4f();
    protected final Vector4f lastColor = new Vector4f();
    @Setter
    protected float lastRotation;

    public TextureObject(Texture texture, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        super(x, y, rotation, scaleX, scaleY);
        this.texture = texture;
        this.lastPosition.set(x, y);
        this.lastRotation = rotation;
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.lastScale.set(scaleX, scaleY);
    }

    public TextureObject(Texture texture, float x, float y, float rotation, float scaleX, float scaleY) {
        this(texture, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(Texture texture, float x, float y, float scaleX, float scaleY) {
        this(texture, x, y, 0.0f, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(Texture texture, float x, float y) {
        this(texture, x, y, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(Texture texture) {
        this(texture, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(float scaleX, float scaleY) {
        this(TextureLoader.dummyTexture, 0.0f, 0.0f, scaleX, scaleY);
    }

    public TextureObject() {
        this(TextureLoader.dummyTexture);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public void clear() {}
}