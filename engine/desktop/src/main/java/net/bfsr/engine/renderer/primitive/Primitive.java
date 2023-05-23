package net.bfsr.engine.renderer.primitive;

import lombok.Getter;
import net.bfsr.engine.Engine;
import org.lwjgl.opengl.GL11;

public class Primitive {
    protected final VAO vao;
    @Getter
    private final int vertexCount;
    @Getter
    private final int indexCount;

    Primitive(int bufferCount, int vertexCount, int indexCount) {
        this.vao = VAO.create(bufferCount);
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    public void renderIndexed() {
        renderIndexed(GL11.GL_TRIANGLES);
    }

    public void renderIndexed(int renderMode) {
        vao.bind();
        GL11.glDrawElements(renderMode, indexCount, GL11.GL_UNSIGNED_INT, 0);
        Engine.renderer.increaseDrawCalls();
    }

    public void clear() {
        vao.clear();
    }
}