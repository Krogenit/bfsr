package net.bfsr.engine.renderer.primitive;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opengl.GL45C.*;
import static org.lwjgl.system.MemoryUtil.memAddress;

public final class VBO {
    @Getter
    private int id;
    private long maxDataSize;

    private VBO(int id) {
        this.id = id;
    }

    public static VBO create() {
        int id = glCreateBuffers();
        return new VBO(id);
    }

    void storeData(ByteBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()), flags, memAddress(data));
    }

    void storeData(LongBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 3, flags, memAddress(data));
    }

    void storeData(FloatBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, memAddress(data));
    }

    void storeData(FloatBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, memAddress(data), onResizeRunnable);
    }

    void storeData(ByteBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()), flags, memAddress(data), onResizeRunnable);
    }

    void storeData(IntBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, memAddress(data), onResizeRunnable);
    }

    void storeData(IntBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, memAddress(data));
    }

    private void storeData(long newDataSize, int flags, long dataAddress, Runnable onResizeRunnable) {
        if (newDataSize <= maxDataSize) {
            nglNamedBufferSubData(id, 0, newDataSize, dataAddress);
        } else {
            if (maxDataSize > 0) {
                glDeleteBuffers(id);
                id = glCreateBuffers();
            }
            onResizeRunnable.run();
            nglNamedBufferStorage(id, newDataSize, dataAddress, flags);
            maxDataSize = newDataSize;
        }
    }

    private void storeData(long newDataSize, int flags, long dataAddress) {
        if (newDataSize <= maxDataSize) {
            nglNamedBufferSubData(id, 0, newDataSize, dataAddress);
        } else {
            if (maxDataSize > 0) {
                glDeleteBuffers(id);
                id = glCreateBuffers();
            }
            nglNamedBufferStorage(id, newDataSize, dataAddress, flags);
            maxDataSize = newDataSize;
        }
    }

    public void clear() {
        glDeleteBuffers(id);
        maxDataSize = 0;
        id = 0;
    }
}