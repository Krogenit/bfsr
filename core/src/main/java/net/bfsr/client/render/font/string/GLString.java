package net.bfsr.client.render.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.VAO;
import net.bfsr.client.render.font.StringRenderer;
import net.bfsr.util.MatrixBufferUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
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
    @Setter
    private FloatBuffer matrixBuffer = new Matrix4f().get(BufferUtils.createFloatBuffer(16));

    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffers();
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

    public void setPosition(Vector2f vector) {
        setPosition(vector.x, vector.y);
    }

    public void setPosition(float x, float y) {
        MatrixBufferUtils.setPosition(matrixBuffer, x, y);
    }

    public void setX(float x) {
        MatrixBufferUtils.setX(matrixBuffer, x);
    }

    public void setY(float y) {
        MatrixBufferUtils.setY(matrixBuffer, y);
    }

    public void clear() {
        vao.clear();
    }
}
