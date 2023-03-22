package net.bfsr.client.renderer.instanced;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;

public final class GUIRenderer {
    private SpriteRenderer spriteRenderer;
    private BuffersHolder buffersHolder;

    public void init(SpriteRenderer spriteRenderer) {
        this.spriteRenderer = spriteRenderer;
        this.buffersHolder = spriteRenderer.buffersHolders[BufferType.GUI.ordinal()];
    }

    public void render() {
        if (buffersHolder.getObjectCount() > 0) {
            spriteRenderer.render(buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    public void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        add(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void addCentered(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        addCentered(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        add(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, 0);
    }

    public void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(x, y, sizeX, sizeY, r, g, b, a, 0);
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a) {
        add(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, 0);
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                    Texture texture) {
        add(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        add(x, y, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float sizeX, float sizeY, float r, float g, float b, float a,
                    Texture texture) {
        add(lastX, lastY, x, y, lastSin, lastCos, sin, cos, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void add(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                    long textureHandle) {
        float sin = LUT.sin(rotation);
        float cos = LUT.cos(rotation);
        spriteRenderer.putVerticesCentered(x, y, sin, cos, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float sizeX, float sizeY, float r, float g, float b, float a,
                    long textureHandle) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        spriteRenderer.putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation,
                lastCos + (cos - lastCos) * interpolation, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                    long textureHandle) {
        float interpolation = Core.get().getRenderer().getInterpolation();
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
        float interpolation = Core.get().getRenderer().getInterpolation();
        float interpolatedX = lastX + (x - lastX) * interpolation;
        float interpolatedY = lastY + (y - lastY) * interpolation;
        spriteRenderer.putVertices(interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, interpolatedY,
                interpolatedX, interpolatedY, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        spriteRenderer.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public static GUIRenderer get() {
        return Core.get().getRenderer().getGuiRenderer();
    }
}