package net.bfsr.client.render;

import org.lwjgl.opengl.GL11C;

import java.nio.IntBuffer;

public class VAOWithIndices extends VAO {
    private int indexCount;
    private VBO indexVbo;

    protected VAOWithIndices(int id, int drawType, int vboCount) {
        super(id, vboCount);
    }

    public void createIndexBuffer(IntBuffer indices, int flags) {
        indexVbo = VBO.create();
        indexVbo.storeData(indices, flags);
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
