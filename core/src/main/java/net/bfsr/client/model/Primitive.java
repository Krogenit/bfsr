package net.bfsr.client.model;

import lombok.Getter;
import net.bfsr.client.render.VAO;
import net.bfsr.core.Core;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Primitive {
    final VAO vao;
    @Getter
    private final int vertexCount;
    @Getter
    private final int indexCount;

    Primitive(int bufferCount, int vertexCount, int indexCount) {
        this.vao = VAO.create(bufferCount);
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    public Primitive(float[] positions) {
        this(1, positions.length / 2, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            vao.createVertexBuffer(0);
            vao.attributeBindingAndFormat(0, 2, 0, 0);
            vao.updateVertexBuffer(0, stack.mallocFloat(positions.length).put(positions).flip(), 0, 8);
            vao.enableAttributes(1);
        }
    }

    public void addInstancedAttribute(int vboIndex, int attribute, int dataSize, int instancedDataLength, int offset) {
        vao.vertexArrayVertexBuffer(vboIndex, instancedDataLength << 2);
        vao.attributeBindingAndFormat(attribute, dataSize, vboIndex, offset << 2);
        vao.attributeDivisor(attribute, 1);
    }

    public void updateVertexBuffer(int index, FloatBuffer data, int instancedDataLength) {
        vao.updateVertexBuffer(index, data, GL44C.GL_DYNAMIC_STORAGE_BIT, instancedDataLength << 2);
    }

    public void updateVertexBuffer(int index, ByteBuffer data, int instancedDataLength) {
        vao.updateVertexBuffer(index, data, GL44C.GL_DYNAMIC_STORAGE_BIT, instancedDataLength << 2);
    }

    public void renderIndexed() {
        renderIndexed(GL11.GL_TRIANGLES);
    }

    public void renderIndexed(int renderMode) {
        vao.bind();
        GL11.glDrawElements(renderMode, indexCount, GL11.GL_UNSIGNED_INT, 0);
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    public void render(int renderMode) {
        vao.bind();
        GL11.glDrawArrays(renderMode, 0, vertexCount);
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    public void clear() {
        vao.clear();
    }

    public int getVaoId() {
        return vao.getId();
    }
}
