package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.GL45C.glCreateBuffers;
import static org.lwjgl.opengl.GL45C.glMapNamedBufferRange;
import static org.lwjgl.opengl.GL45C.glNamedBufferStorage;
import static org.lwjgl.opengl.GL45C.glUnmapNamedBuffer;

@Getter
public class CircularBuffer {
    private long capacity;
    private final ByteBuffer[] buffers;
    private final int[] bufferIds;
    private final long[] bufferAddresses;

    CircularBuffer(int buffering) {
        buffers = new ByteBuffer[buffering];
        bufferIds = new int[buffering];
        bufferAddresses = new long[buffering];
    }

    public void create(long capacity) {
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

    void resize(int capacity) {
        clear();
        create(capacity);
    }

    ByteBuffer getBuffer(int bufferingIndex) {
        return buffers[bufferingIndex];
    }

    long getAddress(int bufferingIndex) {
        return bufferAddresses[bufferingIndex];
    }

    int getBufferId(int bufferingIndex) {
        return bufferIds[bufferingIndex];
    }

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
