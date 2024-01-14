package net.bfsr.engine.renderer.primitive;

import lombok.Getter;
import net.bfsr.engine.Engine;

import static org.lwjgl.opengl.GL11.*;

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

    public void renderIndexed() {
        renderIndexed(GL_TRIANGLES);
    }

    public void renderIndexed(int renderMode) {
        vao.bind();
        glDrawElements(renderMode, indexCount, GL_UNSIGNED_INT, 0);
        Engine.renderer.increaseDrawCalls();
    }

    public void clear() {
        vao.clear();
    }
}