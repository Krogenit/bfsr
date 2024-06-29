package net.bfsr.engine.renderer;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;

public final class GUIRenderer extends AbstractGUIRenderer {
    private AbstractRenderer renderer;
    private AbstractSpriteRenderer spriteRenderer;
    private AbstractBuffersHolder buffersHolder;
    @Getter
    private final EventBus eventBus = new EventBus();

    @Override
    public void init() {
        this.renderer = Engine.renderer;
        this.spriteRenderer = Engine.renderer.spriteRenderer;
        this.buffersHolder = spriteRenderer.getBuffersHolder(BufferType.GUI);
    }

    @Override
    public void render() {
        render(GL11C.GL_QUADS);
    }

    @Override
    public void render(int mode) {
        if (buffersHolder.getObjectCount() > 0) {
            spriteRenderer.render(mode, buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(),
                    buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    @Override
    public void add(float x, float y, float width, float height, Vector4f color) {
        add(x, y, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public void add(float x, float y, float width, float height, float r, float g, float b, float a) {
        add(x, y, width, height, r, g, b, a, 0);
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float width, float height, Vector4f color) {
        add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float width, float height, float r, float g, float b, float a) {
        add(lastX, lastY, x, y, width, height, r, g, b, a, 0);
    }

    @Override
    public void add(float x, float y, float width, float height, float r, float g, float b, float a, AbstractTexture texture) {
        add(x, y, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float x, float y, float width, float height, Vector4f color, AbstractTexture texture) {
        add(x, y, width, height, color.x, color.y, color.z, color.w, texture.getTextureHandle());
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float width, float height, float r, float g, float b, float a,
                    AbstractTexture texture) {
        add(lastX, lastY, x, y, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float width, float height, Vector4f color, AbstractTexture texture) {
        add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w, texture.getTextureHandle());
    }

    @Override
    public void addRotated(float x, float y, float rotation, float width, float height, Vector4f color) {
        addRotated(x, y, rotation, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public void addRotated(float x, float y, float lastRotation, float rotation, float width, float height, Vector4f color) {
        addRotated(x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width, float height,
                           float r, float g, float b, float a) {
        addRotated(lastX, lastY, x, y, lastRotation, rotation, width, height, r, g, b, a, 0);
    }

    @Override
    public void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width, float height,
                           Vector4f color) {
        addRotated(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public void addRotated(float x, float y, float rotation, float width, float height, Vector4f color, AbstractTexture texture) {
        addRotated(x, y, rotation, width, height, color.x, color.y, color.z, color.w, texture.getTextureHandle());
    }

    @Override
    public void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width, float height,
                           Vector4f color, AbstractTexture texture) {
        addRotated(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w,
                texture.getTextureHandle());
    }

    @Override
    public void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width, float height,
                           float r, float g, float b, float a, AbstractTexture texture) {
        addRotated(lastX, lastY, x, y, lastRotation, rotation, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void addRotated(float x, float y, float rotation, float width, float height, float r, float g, float b, float a,
                           AbstractTexture texture) {
        addRotated(x, y, rotation, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void addRotated(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float width,
                           float height, float r, float g, float b, float a, AbstractTexture texture) {
        addRotated(lastX, lastY, x, y, lastSin, lastCos, sin, cos, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void addCentered(float x, float y, float width, float height, float r, float g, float b, float a,
                            AbstractTexture texture) {
        addCentered(x, y, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void addPrimitive(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float r, float g,
                             float b, float a, long textureHandle) {
        spriteRenderer.putVerticesClockWise(x1, y1, x2, y2, x3, y3, x4, y4, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void addRotated(float x, float y, float rotation, float width, float height, float r, float g, float b, float a,
                            long textureHandle) {
        spriteRenderer.putVerticesCenteredClockWise(x, y, LUT.sin(rotation), LUT.cos(rotation), width * 0.5f, height * 0.5f,
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void addRotated(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float width,
                            float height, float r, float g, float b, float a, long textureHandle) {
        float interpolation = renderer.getInterpolation();
        spriteRenderer.putVerticesCenteredClockWise(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation, width * 0.5f, height * 0.5f,
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void addRotated(float x, float y, float lastRotation, float rotation, float width, float height,
                            float r, float g, float b, float a, long textureHandle) {
        float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * renderer.getInterpolation();
        spriteRenderer.putVerticesCenteredClockWise(x, y, LUT.sin(interpolatedRotation), LUT.cos(interpolatedRotation), width * 0.5f,
                height * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width, float height,
                            float r, float g, float b, float a, long textureHandle) {
        float interpolation = renderer.getInterpolation();
        float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        spriteRenderer.putVerticesCenteredClockWise(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                LUT.sin(interpolatedRotation), LUT.cos(interpolatedRotation), width * 0.5f, height * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle) {
        spriteRenderer.putVerticesClockWise(x, height + y, width + x, height + y, width + x, y, x, y, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void addCentered(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle) {
        spriteRenderer.putVerticesCenteredClockWise(x, y, width * 0.5f, height * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    private void add(float lastX, float lastY, float x, float y, float width, float height, float r, float g, float b, float a,
                     long textureHandle) {
        float interpolation = renderer.getInterpolation();
        float interpolatedX = lastX + (x - lastX) * interpolation;
        float interpolatedY = lastY + (y - lastY) * interpolation;
        spriteRenderer.putVerticesClockWise(interpolatedX, height + interpolatedY, width + interpolatedX, height + interpolatedY,
                width + interpolatedX, interpolatedY, interpolatedX, interpolatedY, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }
}