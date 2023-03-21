package net.bfsr.client.renderer.instanced;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;

public final class GUIRenderer {
    public static void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, sizeX, sizeY, r, g, b, a, 0, BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, 0);
    }

    public static void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, textureHandle, BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                                                     BufferType bufferType) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, 0, bufferType);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                                                     Texture texture) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), BufferType.GUI);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                                                     Texture texture, BufferType bufferType) {
        addGUIElementToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), bufferType);
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY, float r, float g, float b, float a,
                                                     long textureHandle, BufferType bufferType) {
        BuffersHolder buffersHolder = SpriteRenderer.INSTANCE.buffersHolders[bufferType.ordinal()];
        float interpolation = Core.get().getRenderer().getInterpolation();
        float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        float sin = LUT.sin(interpolatedRotation);
        float cos = LUT.cos(interpolatedRotation);
        SpriteRenderer.INSTANCE.putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, sin, cos, sizeX * 0.5f, sizeY * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        SpriteRenderer.INSTANCE.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public static void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, BufferType bufferType) {
        BuffersHolder buffersHolder = SpriteRenderer.INSTANCE.buffersHolders[bufferType.ordinal()];
        SpriteRenderer.INSTANCE.putVertices(x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, y, x, y, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        SpriteRenderer.INSTANCE.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public static void addGUIElementToRenderPipeLine(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, BufferType bufferType) {
        BuffersHolder buffersHolder = SpriteRenderer.INSTANCE.buffersHolders[bufferType.ordinal()];
        float interpolation = Core.get().getRenderer().getInterpolation();
        float interpolatedX = lastX + (x - lastX) * interpolation;
        float interpolatedY = lastY + (y - lastY) * interpolation;
        SpriteRenderer.INSTANCE.putVertices(interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, sizeY + interpolatedY, sizeX + interpolatedX, interpolatedY,
                interpolatedX, interpolatedY, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        SpriteRenderer.INSTANCE.putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        SpriteRenderer.INSTANCE.putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }
}