package net.bfsr.client.renderer.primitive;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

@Getter
public class VertexColorBuffer {
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final int vertexCount;

    public VertexColorBuffer(int vertexCount) {
        this.vertexCount = vertexCount;
        vertexBuffer = BufferUtils.createFloatBuffer(vertexCount << 1);
        colorBuffer = BufferUtils.createFloatBuffer(vertexCount << 2);
    }

    public void addVertex(float x, float y) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
    }

    public void addColor(float r, float g, float b, float a) {
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
    }

    public void flipColorBuffer() {
        colorBuffer.flip();
    }

    public void flip() {
        flipVertexBuffer();
        flipColorBuffer();
    }
}
