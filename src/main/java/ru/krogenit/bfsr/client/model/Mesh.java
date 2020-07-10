package ru.krogenit.bfsr.client.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.core.Core;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

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

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			glBindVertexArray(0);
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

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			glBindVertexArray(0);
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

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			vboId = glGenBuffers();
			vboIdList.add(vboId);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

			glBindVertexArray(0);
		} finally {
			if (verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
		}
	}

	protected void initRender() {
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
	}

	protected void endRender() {
//		glDisableVertexAttribArray(0);
//		glDisableVertexAttribArray(1);
//		glDisableVertexAttribArray(2);
//		glBindVertexArray(0);
	}

	public void render() {
		initRender();

		glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
		Core core = Core.getCore();
		Renderer renderer = core.getRenderer();
		renderer.setDrawCalls(renderer.getDrawCalls() + 1);
//		endRender();
	}

	public void clear() {
		glDisableVertexAttribArray(0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for (int vboId : vboIdList) {
			glDeleteBuffers(vboId);
		}

		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
	
	public int getVaoId() {
		return vaoId;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
}
