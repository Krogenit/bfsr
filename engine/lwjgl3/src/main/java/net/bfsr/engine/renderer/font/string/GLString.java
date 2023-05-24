package net.bfsr.engine.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Getter
public class GLString extends AbstractGLString {
    @Setter
    private int width, height;
    private FloatBuffer vertexBuffer;
    private ByteBuffer materialBuffer;

    @Override
    public void init(int glyphCount) {
        vertexBuffer = BufferUtils.createFloatBuffer(glyphCount * AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        materialBuffer = BufferUtils.createByteBuffer(glyphCount * AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void flipBuffers() {
        vertexBuffer.flip();
        materialBuffer.flip();
    }

    @Override
    public void checkBuffers(int dataSize) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < dataSize * AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES) {
            FloatBuffer newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            vertexBuffer.flip();
            newBuffer.put(vertexBuffer);
            vertexBuffer = newBuffer;
        }
        while (materialBuffer.capacity() - materialBuffer.position() < dataSize * AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
            materialBuffer.flip();
            newBuffer.put(materialBuffer);
            materialBuffer = newBuffer;
        }
    }

    @Override
    public void clearBuffers() {
        vertexBuffer.clear();
        materialBuffer.clear();
    }
}