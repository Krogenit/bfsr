package net.bfsr.client.render;

import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class InstancedRenderer {
    public static InstancedRenderer INSTANCE;

    private static final int INSTANCE_DATA_LENGTH = 22;

    private TexturedQuad quad;
    private ByteBuffer buffer = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 512);
    private int instanceCount;

    public InstancedRenderer() {
        INSTANCE = this;
    }

    public void init() {
        quad = TexturedQuad.createParticleCenteredQuad();
        quad.addInstancedAttribute(2, 1, 4, INSTANCE_DATA_LENGTH, 0);
        quad.addInstancedAttribute(2, 2, 4, INSTANCE_DATA_LENGTH, 4);
        quad.addInstancedAttribute(2, 3, 4, INSTANCE_DATA_LENGTH, 8);
        quad.addInstancedAttribute(2, 4, 4, INSTANCE_DATA_LENGTH, 12);
        quad.addInstancedAttribute(2, 5, 4, INSTANCE_DATA_LENGTH, 16);
        quad.addInstancedAttribute(2, 6, 2, INSTANCE_DATA_LENGTH, 20);
    }

    public void render() {
        Core.getCore().getRenderer().getParticleRenderer().getParticleShader().enable();
        quad.updateVertexBuffer(2, buffer.flip(), INSTANCE_DATA_LENGTH);
        GL30C.glBindVertexArray(quad.getVaoId());
        GL31C.glDrawArraysInstanced(GL11C.GL_QUADS, 0, 4, instanceCount);
        Core.getCore().getRenderer().increaseDrawCalls();

        buffer.clear();
        instanceCount = 0;
        Core.getCore().getRenderer().getShader().enable();
    }

    public void addToRenderPipeLine(TextureObject textureObject, float interpolation) {
        storeMatrix(Transformation.getModelMatrix(textureObject, interpolation));
        storeColor(textureObject.getColor());
        storeTextureHandle(textureObject.getTexture().getTextureHandle());
        instanceCount++;
    }

    public void addToRenderPipeLine(FloatBuffer matrix, float r, float g, float b, float a, Texture texture) {
        storeMatrix(matrix);
        storeColor(r, g, b, a);
        storeTextureHandle(texture.getTextureHandle());
        instanceCount++;
    }

    private void storeMatrix(FloatBuffer matrix) {
        for (int j = 0; j < 16; j++) {
            buffer.putFloat(matrix.get(j));
        }
    }

    private void storeColor(Vector4f color) {
        storeColor(color.x, color.y, color.z, color.w);
    }

    private void storeColor(float r, float g, float b, float a) {
        buffer.putFloat(r);
        buffer.putFloat(g);
        buffer.putFloat(b);
        buffer.putFloat(a);
    }

    private void storeTextureHandle(long textureHandle) {
        buffer.putLong(textureHandle);
    }

    private void checkBufferSize(int newDataSize) {
        while (buffer.capacity() - buffer.position() < newDataSize) {
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() << 1);
            if (buffer.position() > 0) newBuffer.put(buffer.flip());
            buffer = newBuffer;
        }
    }
}
