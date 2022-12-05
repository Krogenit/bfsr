package net.bfsr.client.loader;

import net.bfsr.client.font.GUIText;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

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
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    public int createEmptyVbo(int floatCount) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount * 4, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    public void addInstancedAttribute(int vao, int vbo, int attribute, int dataSize, int instancedDataLength, int offset) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL30.glBindVertexArray(vao);
        GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false, instancedDataLength * 4, offset * 4);
        GL33.glVertexAttribDivisor(attribute, 1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void updateVbo(int vbo, float[] data) {
//		buffer.clear();
//		buffer.put(data);
//		buffer.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.length * 4, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private int storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vboID;
    }

    private int storeDataInAttributeListForRendering(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        return vboID;
    }

    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
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
            GL15.glDeleteBuffers(vbo);
        }
        GL30.glDeleteVertexArrays(vao);
    }

    public void deleteVao(int vao) {
        GL30.glDeleteVertexArrays(vao);
    }

    public void deleteVBOs(List<Integer> VBOs) {
        for (Integer vbo : VBOs) {
            deleteVbo(vbo);
        }
    }

    public void deleteVbo(int vbo) {
        GL15.glDeleteBuffers(vbo);
    }

    public int[] getVBOs(int vao) {
        return textsVBOs.get(vao);
    }

}
