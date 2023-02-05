package net.bfsr.client.render;

import net.bfsr.client.render.texture.Texture;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class InstancedRenderer {
    public static InstancedRenderer INSTANCE;

    private static final int START_OBJECT_COUNT = 512;
    private static final int VERTEX_DATA_SIZE = 4;
    private static final int COLOR_AND_TEXTURE_DATA_SIZE = 8;

    private VAO vao;
    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(START_OBJECT_COUNT * VERTEX_DATA_SIZE * 4);
    private ByteBuffer colorAndTextureBuffer = BufferUtils.createByteBuffer(START_OBJECT_COUNT * COLOR_AND_TEXTURE_DATA_SIZE * 4);
    private int instanceCount;

    public InstancedRenderer() {
        INSTANCE = this;
    }

    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffers();
        vao.attributeBindingAndFormat(0, 4, 0, 0);
        vao.enableAttributes(1);
    }

    public void render() {
        if (instanceCount > 0) {
            Core.getCore().getRenderer().getParticleRenderer().getParticleShader().enable();
            vao.bind();
            vao.updateVertexBuffer(0, vertexBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE << 2);
            vao.updateBuffer(1, colorAndTextureBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT);
            vao.bindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, 1);
            GL11C.glDrawArrays(GL11C.GL_QUADS, 0, instanceCount << 2);
            Core.getCore().getRenderer().increaseDrawCalls();

            vertexBuffer.clear();
            colorAndTextureBuffer.clear();
            instanceCount = 0;
        }
    }

    public void addToRenderPipeLine(TextureObject textureObject, float interpolation) {
        storeVertices(textureObject, interpolation);
        storeColor(textureObject.getColor());
        storeTextureHandle(textureObject.getTexture().getTextureHandle());
        instanceCount++;
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, float interpolation) {
        storeVertices(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, interpolation);
        storeColor(r, g, b, a);
        storeTextureHandle(texture.getTextureHandle());
        instanceCount++;
    }

    public void addToRenderPipeLine(float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture) {
        storeVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), scaleX * 0.5f, scaleY * 0.5f);
        storeColor(r, g, b, a);
        storeTextureHandle(texture.getTextureHandle());
        instanceCount++;
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle());
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        storeVertices(x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, y, x, y);
        storeColor(r, g, b, a);
        storeTextureHandle(textureHandle);
        instanceCount++;
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle) {
        storeGuiElementVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), sizeX, sizeY);
        storeColor(r, g, b, a);
        storeTextureHandle(textureHandle);
        instanceCount++;
    }

    private void storeVertices(TextureObject textureObject, float interpolation) {
        storeVertices(textureObject.getLastPosition().x, textureObject.getLastPosition().y, textureObject.getPosition().x, textureObject.getPosition().y, textureObject.getLastRotation(),
                textureObject.getRotation(), textureObject.getLastScale().x, textureObject.getLastScale().y, textureObject.getScale().x, textureObject.getScale().y, interpolation);
    }

    private void storeVertices(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                               float interpolation) {
        final float sizeX = 0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation);
        final float sizeY = 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation);
        final float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        final float sin = LUT.sin(interpolatedRotation);
        final float cos = LUT.cos(interpolatedRotation);
        final float positionX = lastX + (x - lastX) * interpolation;
        final float positionY = lastY + (y - lastY) * interpolation;
        storeVertices(positionX, positionY, sin, cos, sizeX, sizeY);
    }

    private void storeVertices(float x, float y, float sizeX, float sizeY) {
        storeVertices(-sizeX + x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, -sizeY + y, -sizeX + x, -sizeY + y);
    }

    private void storeVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        vertexBuffer.put(x1);
        vertexBuffer.put(y1);
        vertexBuffer.put(u1);
        vertexBuffer.put(v1);
        vertexBuffer.put(x2);
        vertexBuffer.put(y2);
        vertexBuffer.put(u2);
        vertexBuffer.put(v2);
        vertexBuffer.put(x3);
        vertexBuffer.put(y3);
        vertexBuffer.put(u3);
        vertexBuffer.put(v3);
        vertexBuffer.put(x4);
        vertexBuffer.put(y4);
        vertexBuffer.put(u4);
        vertexBuffer.put(v4);
    }

    private void storeVertices(float x, float y, float sin, float cos, float sizeX, float sizeY) {
        final float minusSizeX = -sizeX;
        final float minusSizeY = -sizeY;

        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        final float sinSizeX = sin * sizeX;
        final float cosSizeX = cos * sizeX;
        final float sinSizeY = sin * sizeY;
        final float cosSizeY = cos * sizeY;

        final float x1 = cos * minusSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX - sin * minusSizeY + x;
        final float y3 = sinSizeX + cos * minusSizeY + y;
        final float y1 = sin * minusSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

        vertexBuffer.put(x1);
        vertexBuffer.put(y1);
        vertexBuffer.put(u1);
        vertexBuffer.put(v1);
        vertexBuffer.put(x2);
        vertexBuffer.put(y2);
        vertexBuffer.put(u2);
        vertexBuffer.put(v2);
        vertexBuffer.put(x3);
        vertexBuffer.put(y3);
        vertexBuffer.put(u3);
        vertexBuffer.put(v3);
        vertexBuffer.put(x1 + (x3 - x2));
        vertexBuffer.put(y3 - (y2 - y1));
        vertexBuffer.put(u4);
        vertexBuffer.put(v4);
    }

    private void storeGuiElementVertices(float x, float y, float sin, float cos, float sizeX, float sizeY) {
        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        final float sinSizeX = sin * sizeX;
        final float cosSizeX = cos * sizeX;
        final float sinSizeY = sin * sizeY;
        final float cosSizeY = cos * sizeY;

        final float x1 = -sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX + x;
        final float y3 = sinSizeX + y;
        final float y1 = cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

        vertexBuffer.put(x1);
        vertexBuffer.put(y1);
        vertexBuffer.put(u1);
        vertexBuffer.put(v1);
        vertexBuffer.put(x2);
        vertexBuffer.put(y2);
        vertexBuffer.put(u2);
        vertexBuffer.put(v2);
        vertexBuffer.put(x3);
        vertexBuffer.put(y3);
        vertexBuffer.put(u3);
        vertexBuffer.put(v3);
        vertexBuffer.put(x1 + (x3 - x2));
        vertexBuffer.put(y3 - (y2 - y1));
        vertexBuffer.put(u4);
        vertexBuffer.put(v4);
    }

    private void storeColor(Vector4f color) {
        storeColor(color.x, color.y, color.z, color.w);
    }

    private void storeColor(float r, float g, float b, float a) {
        colorAndTextureBuffer.putFloat(r);
        colorAndTextureBuffer.putFloat(g);
        colorAndTextureBuffer.putFloat(b);
        colorAndTextureBuffer.putFloat(a);
    }

    private void storeTextureHandle(long textureHandle) {
        colorAndTextureBuffer.putLong(textureHandle);
        colorAndTextureBuffer.putLong(0);//padding
    }

    private void checkBufferSize(int newDataSize) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < newDataSize) {
            FloatBuffer newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            if (vertexBuffer.position() > 0) newBuffer.put(vertexBuffer.flip());
            vertexBuffer = newBuffer;
        }
    }
}
