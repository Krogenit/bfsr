package net.bfsr.engine.renderer.primitive;

import lombok.Getter;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public final class VAO {
    @Getter
    private final int id;
    private final VBO[] VBOs;

    private VAO(int id, int vboCount) {
        this.id = id;
        VBOs = new VBO[vboCount];
    }

    public static VAO create(int vboCount) {
        int id = GL45.glCreateVertexArrays();
        return new VAO(id, vboCount);
    }

    public void createVertexBuffers() {
        for (int i = 0; i < VBOs.length; i++) {
            VBOs[i] = VBO.create();
        }
    }

    public void vertexArrayVertexBuffer(int index, int stride) {
        GL45C.glVertexArrayVertexBuffer(id, index, VBOs[index].getId(), 0, stride);
    }

    public void vertexArrayElementBuffer(int index) {
        GL45C.glVertexArrayElementBuffer(id, VBOs[index].getId());
    }

    public void attributeBindingAndFormat(int attribute, int attributeSize, int bufferIndex, int relativeOffset) {
        GL45C.glVertexArrayAttribBinding(id, attribute, bufferIndex);
        GL45C.glVertexArrayAttribFormat(id, attribute, attributeSize, GL11.GL_FLOAT, false, relativeOffset);
    }

    public void attributeDivisor(int attribute, int divisor) {
        GL45C.glVertexArrayBindingDivisor(id, attribute, divisor);
    }

    public void updateVertexBuffer(int index, FloatBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateVertexBuffer(int index, ByteBuffer data, int flags, int stride) {
        VBOs[index].storeData(data, flags, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateIndexBuffer(int index, IntBuffer data, int flags) {
        VBOs[index].storeData(data, flags, () -> vertexArrayElementBuffer(index));
    }

    public void updateBuffer(int index, ByteBuffer data, int flags) {
        VBOs[index].storeData(data, flags);
    }

    public void updateBuffer(int index, FloatBuffer data, int flags) {
        VBOs[index].storeData(data, flags);
    }

    public void updateBuffer(int index, LongBuffer data, int flags) {
        VBOs[index].storeData(data, flags);
    }

    public void bindBuffer(int target, int bufferIndex) {
        GL15C.glBindBuffer(target, VBOs[bufferIndex].getId());
    }

    public void bindBufferBase(int target, int index, int bufferIndex) {
        GL30.glBindBufferBase(target, index, VBOs[bufferIndex].getId());
    }

    public void bind() {
        GL30C.glBindVertexArray(id);
    }

    public void enableAttributes(int count) {
        for (int i = 0; i < count; i++) {
            GL45C.glEnableVertexArrayAttrib(id, i);
        }
    }

    public void clear() {
        GL30C.glDeleteVertexArrays(id);
        for (int i = 0; i < VBOs.length; i++) {
            VBOs[i].clear();
        }
    }
}