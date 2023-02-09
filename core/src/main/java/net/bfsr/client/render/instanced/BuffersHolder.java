package net.bfsr.client.render.instanced;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.util.MutableInt;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.Future;

@Getter
public class BuffersHolder {
    @Setter
    private Future<?> future;
    private FloatBuffer vertexBuffer;
    private ByteBuffer materialBuffer;
    private final MutableInt vertexBufferIndex = new MutableInt();
    private final MutableInt materialBufferIndex = new MutableInt();
    private int objectCount;

    public BuffersHolder(int initialObjectCount) {
        vertexBuffer = BufferUtils.createFloatBuffer(initialObjectCount * InstancedRenderer.VERTEX_DATA_SIZE);
        materialBuffer = BufferUtils.createByteBuffer(initialObjectCount * InstancedRenderer.MATERIAL_DATA_SIZE);
    }

    private void checkBuffersSize(int objectCount) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < objectCount * InstancedRenderer.VERTEX_DATA_SIZE) {
            vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
        }

        while (materialBuffer.capacity() - materialBuffer.position() < objectCount * InstancedRenderer.MATERIAL_DATA_SIZE) {
            materialBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
        }
    }

    public void incrementObjectCount() {
        objectCount++;
    }

    public void addObjectCount(int count) {
        objectCount += count;
    }

    public void clearObjectCount() {
        objectCount = 0;
    }

    public void reset() {
        clearObjectCount();
        vertexBufferIndex.set(0);
        materialBufferIndex.set(0);
        vertexBuffer.clear();
        materialBuffer.clear();
    }
}
