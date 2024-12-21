package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import net.bfsr.engine.renderer.primitive.VBO;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44C.GL_DYNAMIC_STORAGE_BIT;
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
    private VBO vbo;

    CircularBuffer(int buffering) {
        buffers = new ByteBuffer[buffering];
        bufferIds = new int[buffering];
        bufferAddresses = new long[buffering];
    }

    public void create(int capacity) {
        this.capacity = capacity;

        if (buffers.length > 1) {
            int mapFlags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
            int createFlags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;

            for (int i = 0; i < bufferIds.length; i++) {
                bufferIds[i] = glCreateBuffers();
                glNamedBufferStorage(bufferIds[i], capacity, createFlags);
                buffers[i] = glMapNamedBufferRange(bufferIds[i], 0, capacity, mapFlags, buffers[i]);
                bufferAddresses[i] = MemoryUtil.memAddress(buffers[i]);
            }
        } else {
            buffers[0] = MemoryUtil.memAlloc(capacity);
            bufferAddresses[0] = MemoryUtil.memAddress(buffers[0]);
            vbo = VBO.create();
        }
    }

    void resize(int capacity) {
        if (buffers.length > 1) {
            clear();
            create(capacity);
        } else {
            this.capacity = capacity;
            buffers[0] = MemoryUtil.memRealloc(buffers[0], capacity);
            bufferAddresses[0] = MemoryUtil.memAddress(buffers[0]);
        }
    }

    void updateBuffer(int count) {
        vbo.storeData(bufferAddresses[0], (long) count * COMMAND_SIZE_IN_BYTES, GL_DYNAMIC_STORAGE_BIT);
    }

    long getAddress(int bufferingIndex) {
        return buffers.length > 1 ? bufferAddresses[bufferingIndex] : bufferAddresses[0];
    }

    int getBufferId(int bufferingIndex) {
        return buffers.length > 1 ? bufferIds[bufferingIndex] : vbo.getId();
    }

    public void clear() {
        if (buffers.length > 1) {
            for (int i = 0; i < bufferIds.length; i++) {
                if (bufferIds[i] != 0) {
                    glUnmapNamedBuffer(bufferIds[i]);
                    glDeleteBuffers(bufferIds[i]);
                    bufferIds[i] = 0;
                }
            }
        } else {
            MemoryUtil.memFree(buffers[0]);
            vbo.clear();
        }
    }
}
