package net.bfsr.client.render.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.InstancedRenderer;
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
        vertexBuffer = BufferUtils.createFloatBuffer(glyphCount * InstancedRenderer.VERTEX_DATA_SIZE);
        materialBuffer = BufferUtils.createByteBuffer(glyphCount * InstancedRenderer.MATERIAL_DATA_SIZE);
    }

    public void flipBuffers() {
        vertexBuffer.flip();
        materialBuffer.flip();
    }

    public void checkBuffers(int dataSize) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < dataSize * InstancedRenderer.VERTEX_DATA_SIZE) {
            FloatBuffer newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            vertexBuffer.flip();
            newBuffer.put(vertexBuffer);
            vertexBuffer = newBuffer;
        }
        while (materialBuffer.capacity() - materialBuffer.position() < dataSize * InstancedRenderer.MATERIAL_DATA_SIZE) {
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
