package net.bfsr.client.render;

import lombok.Getter;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VBO {
    @Getter
    private final int id;
    @Getter
    private int attributeIndex;
    private long maxDataSize;

    private VBO(int id) {
        this.id = id;
    }

    public static VBO create() {
        int id = GL45C.glCreateBuffers();
        return new VBO(id);
    }

    public void createBufferData(long size) {
        GL45C.nglNamedBufferData(id, size, MemoryUtil.NULL, GL15C.GL_DYNAMIC_DRAW);
        maxDataSize = size;
    }

    public void addInstancedAttribute(int attribute, int dataSize, int instancedDataLength, long offset) {
        GL20C.glVertexAttribPointer(attribute, dataSize, GL11C.GL_FLOAT, false, instancedDataLength << 2, offset << 2);
        GL33C.glVertexAttribDivisor(attribute, 1);
    }

    void storeData(FloatBuffer data, int drawType) {
        long newDataSize = Integer.toUnsignedLong(data.remaining()) << 2;
        if (newDataSize <= maxDataSize) {
            GL45C.nglNamedBufferSubData(id, 0, newDataSize, MemoryUtil.memAddress(data));
        } else {
            GL45C.glNamedBufferStorage(id, data, drawType);
            maxDataSize = newDataSize;
        }
    }

    void storeData(IntBuffer data, int drawType) {
        long newDataSize = Integer.toUnsignedLong(data.remaining()) << 2;
        if (newDataSize <= maxDataSize) {
            GL45C.nglNamedBufferSubData(id, 0, newDataSize, MemoryUtil.memAddress(data));
        } else {
            GL45C.nglNamedBufferData(id, newDataSize, MemoryUtil.memAddress(data), drawType);
            maxDataSize = newDataSize;
        }
    }

    public void clear() {
        GL15C.glDeleteBuffers(id);
        maxDataSize = 0;
    }
}
