package net.bfsr.client.render;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;

import java.nio.IntBuffer;

public class VAOWithIndices extends VAO {
    private int indexCount;
    private VBO indexVbo;

    protected VAOWithIndices(int id, int drawType, int vboCount) {
        super(id, vboCount);
    }

    public void createIndexBuffer(IntBuffer indices) {
        indexVbo = VBO.create();
        indexVbo.storeData(indices, GL15.GL_STATIC_DRAW);
        indexCount = indices.remaining();
    }

    public void drawElements() {
        GL11C.glDrawElements(GL11C.GL_TRIANGLES, indexCount, GL11C.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void clear() {
        super.clear();
        indexVbo.clear();
    }
}
