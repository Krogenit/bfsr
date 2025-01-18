package net.bfsr.engine.renderer.primitive;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL45C.glBindVertexArray;
import static org.lwjgl.opengl.GL45C.glCreateVertexArrays;
import static org.lwjgl.opengl.GL45C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45C.glEnableVertexArrayAttrib;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribBinding;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribFormat;
import static org.lwjgl.opengl.GL45C.glVertexArrayAttribIFormat;
import static org.lwjgl.opengl.GL45C.glVertexArrayBindingDivisor;
import static org.lwjgl.opengl.GL45C.glVertexArrayElementBuffer;
import static org.lwjgl.opengl.GL45C.glVertexArrayVertexBuffer;

public final class VAO implements AbstractVAO {
    @Getter
    private final int id;
    @Getter
    private final VBO[] VBOs;

    private VAO(int id, int vboCount) {
        this.id = id;
        VBOs = new VBO[vboCount];
    }

    public static VAO create(int vboCount) {
        int id = glCreateVertexArrays();
        return new VAO(id, vboCount);
    }

    public void createVertexBuffers() {
        for (int i = 0; i < VBOs.length; i++) {
            VBOs[i] = VBO.create();
        }
    }

    public void vertexArrayVertexBuffer(int index, int bindingIndex, int stride) {
        vertexArrayVertexBufferInternal(bindingIndex, VBOs[index].getId(), stride);
    }

    public void vertexArrayVertexBufferInternal(int bindingIndex, int bufferId, int stride) {
        glVertexArrayVertexBuffer(id, bindingIndex, bufferId, 0, stride);
    }

    public void vertexArrayVertexBuffer(int index, int stride) {
        vertexArrayVertexBuffer(index, index, stride);
    }

    public void vertexArrayElementBuffer(int index) {
        vertexArrayElementBufferInternal(VBOs[index].getId());
    }

    public void vertexArrayElementBufferInternal(int bufferId) {
        glVertexArrayElementBuffer(id, bufferId);
    }

    public void attributeBindingAndFormat(int attribute, int attributeSize, int bindingIndex, int relativeOffset) {
        attributeBindingAndFormat(attribute, attributeSize, bindingIndex, GL_FLOAT, relativeOffset);
    }

    public void attributeBindingAndFormat(int attribute, int attributeSize, int bindingIndex, int type, int relativeOffset) {
        glVertexArrayAttribBinding(id, attribute, bindingIndex);
        if (type == GL_UNSIGNED_INT) {
            glVertexArrayAttribIFormat(id, attribute, attributeSize, type, relativeOffset);
        } else {
            glVertexArrayAttribFormat(id, attribute, attributeSize, type, false, relativeOffset);
        }
    }

    public void attributeDivisor(int attribute, int divisor) {
        glVertexArrayBindingDivisor(id, attribute, divisor);
    }

    public void updateVertexBuffer(int index, FloatBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateVertexBuffer(int index, IntBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateVertexBuffer(int index, int bindingIndex, IntBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, bindingIndex, stride));
    }

    public void updateVertexBuffer(int index, ByteBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateIndexBuffer(int index, IntBuffer data, int flags) {
        VBOs[index].storeData(data, flags, () -> vertexArrayElementBuffer(index));
    }

    @Override
    public void updateBuffer(int index, ByteBuffer data, int flags) {
        VBOs[index].storeData(data, flags);
    }

    public void updateBuffer(int index, ByteBuffer data, int flags, Runnable onResizeRunnable) {
        VBOs[index].storeData(data, flags, onResizeRunnable);
    }

    @Override
    public void updateBuffer(int index, IntBuffer buffer, int flags) {
        VBOs[index].storeData(buffer, flags);
    }

    public void updateBuffer(int index, IntBuffer buffer, int flags, Runnable onResizeRunnable) {
        VBOs[index].storeData(buffer, flags, onResizeRunnable);
    }

    @Override
    public void updateBuffer(int index, FloatBuffer buffer, int flags) {
        VBOs[index].storeData(buffer, flags);
    }

    public void updateBuffer(int index, FloatBuffer buffer, int flags, Runnable onResizeRunnable) {
        VBOs[index].storeData(buffer, flags, onResizeRunnable);
    }

    public void updateBuffer(int index, long address, long fullDataSize, long offset, long newDataSize, int flags) {
        VBOs[index].storeData(address, fullDataSize, offset, newDataSize, flags);
    }

    public void updateBuffer(int index, LongBuffer data, int flags) {
        VBOs[index].storeData(data, flags);
    }

    @Override
    public void bindBuffer(int target, int bufferIndex) {
        VBOs[bufferIndex].bindBuffer(target);
    }

    @Override
    public void bindBufferBase(int target, int index, int bufferIndex) {
        VBOs[bufferIndex].bindBufferBase(target, index);
    }

    public void bind() {
        glBindVertexArray(id);
    }

    public void enableAttributes(int count) {
        for (int i = 0; i < count; i++) {
            glEnableVertexArrayAttrib(id, i);
        }
    }

    @Override
    public AbstractVBO getBuffer(int index) {
        return VBOs[index];
    }

    public void clear() {
        glDeleteVertexArrays(id);
        for (int i = 0; i < VBOs.length; i++) {
            VBOs[i].clear();
        }
    }
}