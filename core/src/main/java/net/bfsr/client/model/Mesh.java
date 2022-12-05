package net.bfsr.client.model;

import net.bfsr.client.render.Renderer;
import net.bfsr.core.Core;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Mesh {
    protected int vaoId;
    protected int vertexCount;
    protected List<Integer> vboIdList = new ArrayList<>();

    public Mesh(float[] positions, float[] textCoords, int[] indices) {
        FloatBuffer verticesBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        int vboId;

        try {
            vertexCount = indices.length;

            verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
            verticesBuffer.put(positions).flip();

            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();

            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();

            vaoId = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textCoordsBuffer, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

            GL30.glBindVertexArray(0);
        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
                MemoryUtil.memFree(indicesBuffer);
                MemoryUtil.memFree(textCoordsBuffer);
            }
        }
    }

    public Mesh(float[] positions, int[] indices) {
        FloatBuffer verticesBuffer = null;
        IntBuffer indicesBuffer = null;
        int vboId;

        try {
            vertexCount = indices.length;

            verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
            verticesBuffer.put(positions).flip();

            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();

            vaoId = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

            GL30.glBindVertexArray(0);
        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public Mesh(float[] positions) {
        FloatBuffer verticesBuffer = null;
        int vboId;

        try {
            vertexCount = positions.length / 2;

            verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
            verticesBuffer.put(positions).flip();

            vaoId = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            vboId = GL15.glGenBuffers();
            vboIdList.add(vboId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

            GL30.glBindVertexArray(0);
        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
            }
        }
    }

    protected void initRender() {
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
    }

    protected void endRender() {
//		glDisableVertexAttribArray(0);
//		glDisableVertexAttribArray(1);
//		glDisableVertexAttribArray(2);
//		glBindVertexArray(0);
    }

    public void render() {
        initRender();

        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
        Core core = Core.getCore();
        Renderer renderer = core.getRenderer();
        renderer.setDrawCalls(renderer.getDrawCalls() + 1);
//		endRender();
    }

    public void clear() {
        GL20.glDisableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            GL15.glDeleteBuffers(vboId);
        }

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
