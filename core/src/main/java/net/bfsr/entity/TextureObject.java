package net.bfsr.entity;

import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.Texture;
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureObject {
    protected Texture texture;
    protected Vector2f position, origin, scale;
    protected Vector4f color;
    protected float rotate;
    protected EnumZoomFactor zoomFactor = EnumZoomFactor.Default;

    public TextureObject(Texture texture, Vector2f pos, float rotate, Vector2f scale, Vector4f color) {
        this.texture = texture;
        this.position = pos;
        this.rotate = rotate;
        this.color = color;
        if (scale != null) this.origin = new Vector2f(-scale.x / 2.0f, -scale.y / 2.0f);
        this.scale = scale;
    }

    public TextureObject(Texture texture, Vector2f pos, float rotate, Vector2f scale) {
        this(texture, pos, rotate, scale, new Vector4f(1, 1, 1, 1));
    }

    public TextureObject(Texture texture, Vector2f pos, Vector2f scale) {
        this(texture, pos, 0, scale, new Vector4f(1, 1, 1, 1));
    }

    public TextureObject(Texture texture, Vector2f pos) {
        this(texture, pos, 0, new Vector2f(texture.getWidth(), texture.getHeight()), new Vector4f(1, 1, 1, 1));
    }

    public TextureObject(Vector2f pos, Vector2f scale) {
        this(null, pos, 0, scale, new Vector4f(1, 1, 1, 1));
    }

    public TextureObject(Vector2f pos) {
        this(null, pos, 0, null, new Vector4f(1, 1, 1, 1));
    }

    public TextureObject() {
        this(null, new Vector2f(), 0, null, new Vector4f(1, 1, 1, 1));
    }

    public void update(double delta) {

    }

    public void render(BaseShader shader) {
        shader.setColor(getColor());
        shader.enableTexture();
        OpenGLHelper.bindTexture(texture.getId());
        shader.setModelViewMatrix(Transformation.getModelViewMatrix(this));
        Renderer.quad.render();
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setScale(float x, float y) {
        this.scale.x = x;
        this.scale.y = y;
    }

    public void setScale(Vector2f scale) {
        this.scale = scale;
    }

    public void setOrigin(float x, float y) {
        this.origin.x = x;
        this.origin.y = y;
    }

    public void setOrigin(Vector2f origin) {
        this.origin = origin;
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.x = r;
        this.color.y = g;
        this.color.z = b;
        this.color.w = a;
    }

    public void setColor(Vector4f color) {
        this.color = color;
    }

    public Vector2f getPosition() {
        return position;
    }

    public float getRotation() {
        return rotate;
    }

    public Vector2f getScale() {
        return scale;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector4f getColor() {
        return color;
    }

    public void setZoomFactor(EnumZoomFactor zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public EnumZoomFactor getZoomFactor() {
        return zoomFactor;
    }

    public void setRotate(float rotate) {
        this.rotate = rotate;
    }

    public void clear() {
        if (texture != null) texture.delete();
    }
}
