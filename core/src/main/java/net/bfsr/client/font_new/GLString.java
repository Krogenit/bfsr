package net.bfsr.client.font_new;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.VAO;
import org.lwjgl.opengl.GL43;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

@Getter
public class GLString {
    private VAO vao;
    @Setter
    private int vertexCount;
    @Setter
    private int width, height;

    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffer(0);
        vao.createVertexBuffer(1);
        vao.vertexArrayVertexBuffer(0, StringRenderer.VERTEX_DATA_SIZE);
        vao.attributeBindingAndFormat(0, 4, 0, 0);
        vao.attributeBindingAndFormat(1, 4, 0, 16);
        vao.enableAttributes(2);
    }

    public void fillBuffer(FloatBuffer vertexBuffer, LongBuffer textureBuffer) {
        fillBuffer(vertexBuffer, textureBuffer, 0);
    }

    void fillBuffer(FloatBuffer vertexBuffer, LongBuffer textureBuffer, int flags) {
        vao.updateVertexBuffer(0, vertexBuffer, flags, StringRenderer.VERTEX_DATA_SIZE);
        vao.updateBuffer(1, textureBuffer, flags);
        vertexCount = vertexBuffer.remaining() / (StringRenderer.VERTEX_DATA_SIZE / 4);
    }

    public void bind() {
        vao.bind();
        vao.bindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, 1);
    }

    public void clear() {
        vao.clear();
    }
}
