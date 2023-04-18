package net.bfsr.client.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Getter
public class GLString {
    @Setter
    private int width, height;
    private FloatBuffer vertexBuffer;
    private ByteBuffer materialBuffer;

    public void init(int glyphCount) {
        vertexBuffer = BufferUtils.createFloatBuffer(glyphCount * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        materialBuffer = BufferUtils.createByteBuffer(glyphCount * SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES);
    }

    public void flipBuffers() {
        vertexBuffer.flip();
        materialBuffer.flip();
    }

    public void checkBuffers(int dataSize) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < dataSize * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES) {
            FloatBuffer newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            vertexBuffer.flip();
            newBuffer.put(vertexBuffer);
            vertexBuffer = newBuffer;
        }
        while (materialBuffer.capacity() - materialBuffer.position() < dataSize * SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
            materialBuffer.flip();
            newBuffer.put(materialBuffer);
            materialBuffer = newBuffer;
        }
    }

    public void clearBuffers() {
        vertexBuffer.clear();
        materialBuffer.clear();
    }
}