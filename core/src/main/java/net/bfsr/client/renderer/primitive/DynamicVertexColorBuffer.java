package net.bfsr.client.renderer.primitive;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

@Getter
public class DynamicVertexColorBuffer {
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private int vertexCount;

    public DynamicVertexColorBuffer(int initialVertexCount) {
        vertexCount = initialVertexCount;
        vertexBuffer = BufferUtils.createFloatBuffer(initialVertexCount << 1);
        colorBuffer = BufferUtils.createFloatBuffer(initialVertexCount << 2);
    }

    public void addVertex(float x, float y) {
        if (vertexBuffer.position() == vertexBuffer.capacity()) {
            resize();
        }
        vertexBuffer.put(x);
        vertexBuffer.put(y);
    }

    public void addColor(float r, float g, float b, float a) {
        if (colorBuffer.position() == colorBuffer.capacity()) {
            resize();
        }
        colorBuffer.put(r);
        colorBuffer.put(g);
        colorBuffer.put(b);
        colorBuffer.put(a);
    }

    public void addVertex(float x, float y, float r, float g, float b, float a) {
        addVertex(x, y);
        addColor(r, g, b, a);
    }

    public void updateVertexData(int index, float value) {
        vertexBuffer.position(index);
        vertexBuffer.put(value);
    }

    public void resetVertexBufferPosition() {
        vertexBuffer.position(0);
    }

    public void flipVertexBuffer() {
        vertexBuffer.flip();
        vertexCount = vertexBuffer.remaining() / 2;
    }

    public void flipColorBuffer() {
        colorBuffer.flip();
    }

    public void flip() {
        flipVertexBuffer();
        flipColorBuffer();
    }

    private void resize() {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(this.vertexBuffer.capacity() << 1);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(this.colorBuffer.capacity() << 1);
        vertexBuffer.put(this.vertexBuffer);
        colorBuffer.put(this.colorBuffer);
        this.vertexBuffer = vertexBuffer;
        this.colorBuffer = colorBuffer;
    }

    public void reset() {
        vertexBuffer.clear();
        colorBuffer.clear();
        vertexCount = 0;
    }
}
