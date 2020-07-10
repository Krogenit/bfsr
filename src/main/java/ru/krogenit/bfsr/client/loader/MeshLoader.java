package ru.krogenit.bfsr.client.loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;

import ru.krogenit.bfsr.client.font.GUIText;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

public class MeshLoader {

	private final HashMap<Integer, int[]> textsVBOs = new HashMap<>();

	public int loadToVAO(float[] positions, int dimensions) {
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimensions, positions);
		unbindVAO();
		return vaoID;
	}
	
	public int loadToVAOForRendering(float[] positions, float[] textureCoords) {
		int vaoID = createVAO();
		int[] VBOs = new int[2];
		VBOs[0] = storeDataInAttributeList(0, 2, positions);
		VBOs[1] = storeDataInAttributeList(1, 2, textureCoords);
		textsVBOs.put(vaoID, VBOs);
		return vaoID;
	}
	
	public int loadToVAO(GUIText text, float[] positions, float[] textureCoords) {
		int vaoID = createVAO();
		int[] vbos = new int[2];
		vbos[0] = storeDataInAttributeList(0, 2, positions);
		vbos[1] = storeDataInAttributeList(1, 2, textureCoords);
		textsVBOs.put(vaoID, vbos);
		unbindVAO();
		return vaoID;
	}

	private int createVAO() {
		int vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		return vaoID;
	}
	
	public int createEmptyVbo(int floatCount) {
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, floatCount * 4, GL_STREAM_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
	}
	
	public void addInstancedAttribute(int vao, int vbo, int attribute, int dataSize, int instancedDataLength, int offset) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindVertexArray(vao);
		glVertexAttribPointer(attribute, dataSize, GL_FLOAT, false, instancedDataLength * 4, offset * 4);
		glVertexAttribDivisor(attribute, 1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public void updateVbo(int vbo, float[] data) {
//		buffer.clear();
//		buffer.put(data);
//		buffer.flip();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, data.length * 4, GL_STREAM_DRAW);
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private int storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vboID;
	}
	
	private int storeDataInAttributeListForRendering(int attributeNumber, int coordinateSize, float[] data) {
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
		return vboID;
	}

	private void unbindVAO() {
		glBindVertexArray(0);
	}

	private void bindIndicesBuffer(int[] indices) {
		int vboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
	}

	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	private FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public void removeVao(int vao) {
		int[] vbos = textsVBOs.remove(vao);
		for (int vbo : vbos) {
			glDeleteBuffers(vbo);
		}
		glDeleteVertexArrays(vao);
	}

	public void deleteVao(int vao) {
		glDeleteVertexArrays(vao);
	}
	
	public void deleteVBOs(List<Integer> VBOs) {
		for(Integer vbo : VBOs) {
			deleteVbo(vbo);
		}
	}
	
	public void deleteVbo(int vbo) {
		glDeleteBuffers(vbo);
	}
	
	public int[] getVBOs(int vao) {
		return textsVBOs.get(vao);
	}

}
