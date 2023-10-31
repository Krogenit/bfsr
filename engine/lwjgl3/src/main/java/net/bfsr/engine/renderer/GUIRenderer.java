package net.bfsr.engine.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.lwjgl.opengl.GL11C;

public final class GUIRenderer extends AbstractGUIRenderer {
    private AbstractRenderer renderer;
    private AbstractSpriteRenderer spriteRenderer;
    private AbstractBuffersHolder buffersHolder;

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
            spriteRenderer.render(mode, buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    @Override
    public void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture) {
        add(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void addCentered(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture) {
        addCentered(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture) {
        add(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, 0);
    }

    @Override
    public void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(x, y, sizeX, sizeY, r, g, b, a, 0);
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, 0);
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                    AbstractTexture texture) {
        add(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture) {
        add(x, y, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float sizeX, float sizeY, float r,
                    float g, float b, float a, AbstractTexture texture) {
        add(lastX, lastY, x, y, lastSin, lastCos, sin, cos, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                    long textureHandle) {
        spriteRenderer.putVerticesCentered(x, y, LUT.sin(rotation), LUT.cos(rotation), sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float sizeX, float sizeY, float r,
                    float g, float b, float a, long textureHandle) {
        float interpolation = renderer.getInterpolation();
        spriteRenderer.putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation,
                lastCos + (cos - lastCos) * interpolation, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g,
                    float b, float a, long textureHandle) {
        float interpolation = renderer.getInterpolation();
        float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        float sin = LUT.sin(interpolatedRotation);
        float cos = LUT.cos(interpolatedRotation);
        spriteRenderer.putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, sin, cos, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        spriteRenderer.putVertices(x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, y, x, y, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addCentered(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        spriteRenderer.putVerticesCentered(x, y, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        float interpolation = renderer.getInterpolation();
        float interpolatedX = lastX + (x - lastX) * interpolation;
        float interpolatedY = lastY + (y - lastY) * interpolation;
        spriteRenderer.putVertices(interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, interpolatedY,
                interpolatedX, interpolatedY, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void addPrimitive(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float r, float g, float b, float a, long textureHandle) {
        spriteRenderer.putVertices(x1, y1, x2, y2, x3, y3, x4, y4, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }
}