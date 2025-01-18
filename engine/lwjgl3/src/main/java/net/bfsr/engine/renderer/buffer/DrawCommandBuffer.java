package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import net.bfsr.engine.renderer.primitive.VBO;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_INSTANCE_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_VERTEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FIRST_INDEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.INSTANCE_COUNT_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.QUAD_INDEX_COUNT;
import static org.lwjgl.opengl.GL44C.GL_DYNAMIC_STORAGE_BIT;

public class DrawCommandBuffer {
    @Getter
    protected long capacity;
    private ByteBuffer byteBuffer;
    private long address;
    private VBO vbo;

    public void create(int capacity) {
        this.capacity = capacity;

        byteBuffer = MemoryUtil.memAlloc(capacity);
        address = MemoryUtil.memAddress(byteBuffer);
        vbo = VBO.create();
    }

    void resize(int capacity) {
        this.capacity = capacity;
        byteBuffer = MemoryUtil.memRealloc(byteBuffer, capacity);
        address = MemoryUtil.memAddress(byteBuffer);
    }

    public void fill() {
        for (int j = 0; j < capacity; j += COMMAND_SIZE_IN_BYTES) {
            putCommandData(address, j, QUAD_INDEX_COUNT);
            putCommandData(address, j + INSTANCE_COUNT_OFFSET, 1);
            putCommandData(address, j + FIRST_INDEX_OFFSET, 0);
            putCommandData(address, j + BASE_VERTEX_OFFSET, 0);
            putCommandData(address, j + BASE_INSTANCE_OFFSET, 0);
        }
    }

    void putCommandData(long address, int offset, int value) {
        MemoryUtil.memPutInt(address + (offset & 0xFFFF_FFFFL), value);
    }

    void updateBuffer(int count) {
        vbo.storeData(address, (long) count * COMMAND_SIZE_IN_BYTES, GL_DYNAMIC_STORAGE_BIT);
    }

    public void switchBufferingIndex() {}

    public void lockRange() {}

    public void waitForLockedRange() {}

    long getAddress() {
        return address;
    }

    int getBufferId() {
        return vbo.getId();
    }

    public void clear() {
        MemoryUtil.memFree(byteBuffer);
        vbo.clear();
    }
}
