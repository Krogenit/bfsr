package net.bfsr.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;

public class VAO {
    private final int id;
    private final VBO[] VBOs;

    protected VAO(int id, int vboCount) {
        this.id = id;
        VBOs = new VBO[vboCount];
    }

    public static VAO create(int vboCount) {
        int id = GL45.glCreateVertexArrays();
        return new VAO(id, vboCount);
    }

    public VBO createAttribute(int attribute, int attrSize, int bufferIndex) {
        VBO vbo = VBO.create();
        GL45.glVertexArrayAttribBinding(id, attribute, bufferIndex);
        GL45.glVertexArrayAttribFormat(id, attribute, attrSize, GL11.GL_FLOAT, false, 0);
        GL45.glVertexArrayVertexBuffer(id, bufferIndex, vbo.getId(), 0, attrSize << 2);
        VBOs[attribute] = vbo;
        return vbo;
    }

    public VBO createAttribute(int attribute, FloatBuffer data, int attrSize, int drawType) {
        VBO vbo = VBO.create();
        vbo.storeData(data, drawType);
        GL45.glVertexArrayAttribFormat(id, attribute, attrSize, GL11.GL_FLOAT, false, attrSize << 2);
        VBOs[attribute] = vbo;
        return vbo;
    }

    public void createAttribute(int attribute, FloatBuffer data, int attrSize) {
        createAttribute(attribute, data, attrSize, 0);
    }

    public void updateAttribute(int index, FloatBuffer data, int drawType) {
        VBO vbo = VBOs[index];
        vbo.storeData(data, drawType);
    }

    public void bind() {
        GL30C.glBindVertexArray(id);
    }

    public void unbind() {
        GL30C.glBindVertexArray(0);
    }

    public void bindAttribs() {
        for (int i = 0; i < VBOs.length; i++) {
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
