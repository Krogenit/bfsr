package net.bfsr.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.math.ModelMatrixType;
import net.bfsr.math.ModelMatrixUtils;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureObject {
    @Getter
    @Setter
    protected Texture texture;
    @Getter
    protected Vector2f position = new Vector2f();
    protected Vector2f origin = new Vector2f();
    @Getter
    protected Vector2f scale = new Vector2f();
    @Getter
    protected Vector2f lastPosition = new Vector2f();
    @Getter
    protected Vector4f color = new Vector4f();
    @Getter
    @Setter
    protected float rotation;
    @Getter
    private ModelMatrixType modelMatrixType = ModelMatrixType.DEFAULT;

    public TextureObject(Texture texture, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        this.texture = texture;
        this.position.set(x, y);
        this.lastPosition.set(x, y);
        this.rotation = rotation;
        this.color.set(r, g, b, a);
        this.scale.set(scaleX, scaleY);
        this.origin.set(-scaleX / 2.0f, -scaleY / 2.0f);
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

    public void update() {

    }

    public void render(BaseShader shader) {
        render(shader, 1.0f);
    }

    public void render(BaseShader shader, float interpolation) {
        shader.setColor(color.x, color.y, color.y, color.w);
        shader.enableTexture();
        OpenGLHelper.bindTexture(texture.getId());
        shader.setModelMatrix(ModelMatrixUtils.getModelMatrixBuffer(this, interpolation));
        Renderer.centeredQuad.renderIndexed();
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

    public TextureObject setModelMatrixType(ModelMatrixType modelMatrixType) {
        this.modelMatrixType = modelMatrixType;
        return this;
    }

    public void clear() {

    }
}
