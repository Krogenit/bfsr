package net.bfsr.engine.renderer.primitive;

import lombok.Getter;
import net.bfsr.engine.util.RunnableUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opengl.GL45C.glCreateBuffers;
import static org.lwjgl.opengl.GL45C.glDeleteBuffers;
import static org.lwjgl.opengl.GL45C.nglNamedBufferStorage;
import static org.lwjgl.opengl.GL45C.nglNamedBufferSubData;
import static org.lwjgl.system.MemoryUtil.memAddress;

public final class VBO implements AbstractVBO {
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
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()), flags);
    }

    void storeData(LongBuffer data, int flags) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()) << 3, flags);
    }

    public void storeData(FloatBuffer data, int flags) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()) << 2, flags);
    }

    void storeData(FloatBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()) << 2, flags, onResizeRunnable);
    }

    void storeData(ByteBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()), flags, onResizeRunnable);
    }

    void storeData(IntBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()) << 2, flags, onResizeRunnable);
    }

    public void storeData(IntBuffer data, int flags) {
        storeData(memAddress(data), Integer.toUnsignedLong(data.remaining()) << 2, flags);
    }

    private void storeData(long address, long fullDataSize, int flags, Runnable onResizeRunnable) {
        storeData(address, fullDataSize, 0, fullDataSize, flags, onResizeRunnable);
    }

    private void storeData(long address, long fullDataSize, long offset, long newDataSize, int flags, Runnable onResizeRunnable) {
        if (newDataSize + offset <= maxDataSize) {
            nglNamedBufferSubData(id, offset, newDataSize, address + offset);
        } else {
            if (maxDataSize > 0) {
                glDeleteBuffers(id);
                id = glCreateBuffers();
            }
            onResizeRunnable.run();
            nglNamedBufferStorage(id, fullDataSize, address, flags);
            maxDataSize = fullDataSize;
        }
    }

    @Override
    public void storeData(long address, long newDataSize, int flags) {
        storeData(address, newDataSize, 0, newDataSize, flags);
    }

    void storeData(long address, long fullDataSize, long offset, long newDataSize, int flags) {
        storeData(address, fullDataSize, offset, newDataSize, flags, RunnableUtils.EMPTY_RUNNABLE);
    }

    public void clear() {
        glDeleteBuffers(id);
        maxDataSize = 0;
        id = 0;
    }
}