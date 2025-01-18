package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_INSTANCE_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_VERTEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FIRST_INDEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.INSTANCE_COUNT_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.QUAD_INDEX_COUNT;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.GL45C.glCreateBuffers;
import static org.lwjgl.opengl.GL45C.glMapNamedBufferRange;
import static org.lwjgl.opengl.GL45C.glNamedBufferStorage;
import static org.lwjgl.opengl.GL45C.glUnmapNamedBuffer;

@Getter
public class PersistentDrawCommandBuffer extends DrawCommandBuffer {
    /**
     * OpenGL fence sync toggle
     */
    private static final boolean SYNC = false;
    private static final int BUFFERING = 3;

    private final ByteBuffer[] buffers = new ByteBuffer[BUFFERING];
    private final int[] bufferIds = new int[BUFFERING];
    private final long[] bufferAddresses = new long[BUFFERING];
    private int bufferingIndex;

    private final LockManager lockManager = SYNC ? new LockManager() : new DisableSyncLockManager();

    @Override
    public void create(int capacity) {
        this.capacity = capacity;

        int mapFlags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
        int createFlags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;

        for (int i = 0; i < bufferIds.length; i++) {
            bufferIds[i] = glCreateBuffers();
            glNamedBufferStorage(bufferIds[i], capacity, createFlags);
            buffers[i] = glMapNamedBufferRange(bufferIds[i], 0, capacity, mapFlags, buffers[i]);
            bufferAddresses[i] = MemoryUtil.memAddress(buffers[i]);
        }
    }

    @Override
    void resize(int capacity) {
        clear();
        create(capacity);
    }

    @Override
    public void fill() {
        for (int i = 0; i < buffers.length; i++) {
            long address = bufferAddresses[i];

            for (int j = 0; j < capacity; j += COMMAND_SIZE_IN_BYTES) {
                putCommandData(address, j, QUAD_INDEX_COUNT);
                putCommandData(address, j + INSTANCE_COUNT_OFFSET, 1);
                putCommandData(address, j + FIRST_INDEX_OFFSET, 0);
                putCommandData(address, j + BASE_VERTEX_OFFSET, 0);
                putCommandData(address, j + BASE_INSTANCE_OFFSET, 0);
            }
        }
    }

    @Override
    void updateBuffer(int count) {}

    @Override
    public void switchBufferingIndex() {
        bufferingIndex = (bufferingIndex + 1) % BUFFERING;
    }

    @Override
    public void lockRange() {
        lockManager.lockRange(bufferingIndex);
    }

    @Override
    public void waitForLockedRange() {
        lockManager.waitForLockedRange(bufferingIndex);
    }

    @Override
    long getAddress() {
        return bufferAddresses[bufferingIndex];
    }

    @Override
    int getBufferId() {
        return bufferIds[bufferingIndex];
    }

    @Override
    public void clear() {
        for (int i = 0; i < bufferIds.length; i++) {
            if (bufferIds[i] != 0) {
                glUnmapNamedBuffer(bufferIds[i]);
                glDeleteBuffers(bufferIds[i]);
                bufferIds[i] = 0;
            }
        }
    }
}
