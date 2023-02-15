package net.bfsr.client.renderer.primitive;

import lombok.Getter;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public final class VBO {
    @Getter
    private int id;
    private long maxDataSize;

    private VBO(int id) {
        this.id = id;
    }

    public static VBO create() {
        int id = GL45C.glCreateBuffers();
        return new VBO(id);
    }

    void storeData(ByteBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()), flags, MemoryUtil.memAddress(data));
    }

    void storeData(LongBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 3, flags, MemoryUtil.memAddress(data));
    }

    void storeData(FloatBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, MemoryUtil.memAddress(data));
    }

    void storeData(FloatBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, MemoryUtil.memAddress(data), onResizeRunnable);
    }

    void storeData(ByteBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()), flags, MemoryUtil.memAddress(data), onResizeRunnable);
    }

    void storeData(IntBuffer data, int flags, Runnable onResizeRunnable) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, MemoryUtil.memAddress(data), onResizeRunnable);
    }

    void storeData(IntBuffer data, int flags) {
        storeData(Integer.toUnsignedLong(data.remaining()) << 2, flags, MemoryUtil.memAddress(data));
    }

    private void storeData(long newDataSize, int flags, long dataAddress, Runnable onResizeRunnable) {
        if (newDataSize <= maxDataSize) {
            GL45C.nglNamedBufferSubData(id, 0, newDataSize, dataAddress);
        } else {
            if (maxDataSize > 0) {
                GL15C.glDeleteBuffers(id);
                id = GL45C.glCreateBuffers();
            }
            onResizeRunnable.run();
            GL45C.nglNamedBufferStorage(id, newDataSize, dataAddress, flags);
            maxDataSize = newDataSize;
        }
    }

    private void storeData(long newDataSize, int flags, long dataAddress) {
        if (newDataSize <= maxDataSize) {
            GL45C.nglNamedBufferSubData(id, 0, newDataSize, dataAddress);
        } else {
            if (maxDataSize > 0) {
                GL15C.glDeleteBuffers(id);
                id = GL45C.glCreateBuffers();
            }
            GL45C.nglNamedBufferStorage(id, newDataSize, dataAddress, flags);
            maxDataSize = newDataSize;
        }
    }

    public void clear() {
        GL15C.glDeleteBuffers(id);
        maxDataSize = 0;
        id = 0;
    }
}
