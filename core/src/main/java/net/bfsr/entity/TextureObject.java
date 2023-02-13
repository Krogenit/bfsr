package net.bfsr.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Getter
public class TextureObject {
    @Setter
    protected Texture texture;
    protected final Vector2f position = new Vector2f();
    protected final Vector2f scale = new Vector2f();
    protected final Vector2f lastScale = new Vector2f();
    protected final Vector2f lastPosition = new Vector2f();
    protected final Vector4f color = new Vector4f();
    protected final Vector4f lastColor = new Vector4f();
    @Setter
    protected float lastRotation, rotation;

    public TextureObject(Texture texture, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        this.texture = texture;
        this.position.set(x, y);
        this.lastPosition.set(x, y);
        this.rotation = rotation;
        this.lastRotation = rotation;
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.scale.set(scaleX, scaleY);
        this.lastScale.set(scaleX, scaleY);
    }

    public TextureObject(Texture texture, float x, float y, float rotation, float scaleX, float scaleY) {
        this(texture, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(Texture texture, float x, float y, float scaleX, float scaleY) {
        this(texture, x, y, 0.0f, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(Texture texture, float x, float y) {
        this(texture, x, y, 0.0f, texture.getWidth(), texture.getHeight(), 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(float x, float y, float scaleX, float scaleY) {
        this(null, x, y, 0.0f, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(float x, float y) {
        this(null, x, y, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject(TextureRegister texture) {
        this(TextureLoader.getTexture(texture), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public TextureObject() {
        this(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void update() {}

    public void render() {
        SpriteRenderer.INSTANCE.addToRenderPipeLine(this, BufferType.GUI);
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void setScale(float x, float y) {
        this.scale.set(x, y);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public void clear() {}
}
