package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.common.util.MutableInt;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Getter
public class BuffersHolder extends AbstractBuffersHolder {
    @Setter
    private Future<?> future = CompletableFuture.completedFuture(null);
    private FloatBuffer vertexBuffer;
    private ByteBuffer materialBuffer;
    private final MutableInt vertexBufferIndex = new MutableInt();
    private final MutableInt materialBufferIndex = new MutableInt();
    private int objectCount;

    public BuffersHolder(int initialObjectCount) {
        vertexBuffer = BufferUtils.createFloatBuffer(initialObjectCount * AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        materialBuffer = BufferUtils.createByteBuffer(initialObjectCount * AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void checkBuffersSize(int objectCount) {
        while (vertexBuffer.capacity() - vertexBufferIndex.get() < objectCount * AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES) {
            vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
        }

        while (materialBuffer.capacity() - materialBufferIndex.get() < objectCount * AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
            materialBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
        }
    }

    @Override
    public void incrementObjectCount() {
        objectCount++;
    }

    @Override
    public void addObjectCount(int count) {
        objectCount += count;
    }

    @Override
    public void reset() {
        objectCount = 0;
        vertexBufferIndex.set(0);
        materialBufferIndex.set(0);
        vertexBuffer.clear();
        materialBuffer.clear();
    }
}