package net.bfsr.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

public class VAO {
    private final int id;
    private final VBO[] VBOs;

    VAO(int id, int vboCount) {
        this.id = id;
        VBOs = new VBO[vboCount];
    }

    public static VAO create(int vboCount) {
        int id = GL45.glCreateVertexArrays();
        return new VAO(id, vboCount);
    }

    public void createVertexBuffer(int index) {
        VBO vbo = VBO.create();
        VBOs[index] = vbo;
    }

    public void vertexArrayVertexBuffer(int index, int stride) {
        GL45.glVertexArrayVertexBuffer(id, index, VBOs[index].getId(), 0, stride);
    }

    public void attributeBindingAndFormat(int attribute, int attributeSize, int bufferIndex, int relativeOffset) {
        GL45.glVertexArrayAttribBinding(id, attribute, bufferIndex);
        GL45.glVertexArrayAttribFormat(id, attribute, attributeSize, GL11.GL_FLOAT, false, relativeOffset);
    }

    private void createAttribute(int attribute, FloatBuffer data, int attrSize, int drawType) {
        VBO vbo = VBO.create();
        vbo.storeData(data, drawType);
        GL45.glVertexArrayAttribFormat(id, attribute, attrSize, GL11.GL_FLOAT, false, attrSize << 2);
        VBOs[attribute] = vbo;
    }

    public void createAttribute(int attribute, FloatBuffer data, int attrSize) {
        createAttribute(attribute, data, attrSize, 0);
    }

    public void updateVertexBuffer(int index, FloatBuffer data, int drawType, int stride) {
        VBOs[index].storeData(data, drawType, () -> vertexArrayVertexBuffer(index, stride));
    }

    public void updateBuffer(int index, FloatBuffer data, int drawType) {
        VBOs[index].storeData(data, drawType);
    }

    public void updateBuffer(int index, LongBuffer data, int drawType) {
        VBOs[index].storeData(data, drawType);
    }

    public void bindBufferBase(int target, int index, int bufferIndex) {
        GL30.glBindBufferBase(target, index, VBOs[bufferIndex].getId());
    }

    public void bind() {
        GL30C.glBindVertexArray(id);
    }

    public void enableAttributes(int count) {
        for (int i = 0; i < count; i++) {
            GL45.glEnableVertexArrayAttrib(id, i);
        }
    }

    public void clear() {
        GL30.glDeleteVertexArrays(id);
        for (int i = 0; i < VBOs.length; i++) {
            VBOs[i].clear();
        }
    }
}
