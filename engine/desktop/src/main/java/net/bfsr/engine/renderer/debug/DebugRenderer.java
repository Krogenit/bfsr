package net.bfsr.engine.renderer.debug;

import net.bfsr.common.util.AABB;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.primitive.VAO;
import net.bfsr.engine.renderer.shader.DebugShader;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class DebugRenderer extends AbstractDebugRenderer {
    private static final int VERTEX_DATA_SIZE = 6;
    private static final int VERTEX_DATA_SIZE_IN_BYTES = VERTEX_DATA_SIZE << 2;
    private static final int COMMAND_SIZE_IN_BYTES = 16;

    private static final Vector4f AABB_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.1f);

    private final DebugShader debugShader = new DebugShader();
    private VAO vao;
    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(16);
    private ByteBuffer cmdBuffer = BufferUtils.createByteBuffer(16);
    private int objectsCount;

    @Override
    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffers();
        vao.attributeBindingAndFormat(0, 2, 0, 0);
        vao.attributeBindingAndFormat(1, 4, 0, 8);
        vao.enableAttributes(2);
        debugShader.load();
        debugShader.init();
    }

    @Override
    public void bind() {
        vao.bind();
        debugShader.enable();
    }

    @Override
    public void renderAABB(AABB aabb) {
        addCommand(4);
        addVertex(aabb.getMinX(), aabb.getMinY(), AABB_COLOR);
        addVertex(aabb.getMinX(), aabb.getMaxY(), AABB_COLOR);
        addVertex(aabb.getMaxX(), aabb.getMaxY(), AABB_COLOR);
        addVertex(aabb.getMaxX(), aabb.getMinY(), AABB_COLOR);
    }

    @Override
    public void render(int type) {
        vao.updateVertexBuffer(0, vertexBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE_IN_BYTES);
        vao.updateBuffer(1, cmdBuffer.flip(), GL44C.GL_DYNAMIC_STORAGE_BIT);
        vao.bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, 1);
        GL43.glMultiDrawArraysIndirect(type, 0, objectsCount, 0);
        Engine.renderer.increaseDrawCalls();
    }

    @Override
    public void addCommand(int count) {
        checkBuffersSize(count);
        cmdBuffer.putInt(count);
        cmdBuffer.putInt(1);//instance count
        cmdBuffer.putInt(vertexBuffer.position() / VERTEX_DATA_SIZE);//first vertex index
        cmdBuffer.putInt(objectsCount++);//base instance
    }

    @Override
    public void addVertex(float x, float y, Vector4f color) {
        addVertex(x, y, color.x, color.y, color.z, color.w);
    }

    public void addVertex(float x, float y, float r, float g, float b, float a) {
        vertexBuffer.put(x).put(y).put(r).put(g).put(b).put(a);
    }

    void checkBuffersSize(int vertexCount) {
        while (vertexBuffer.capacity() - vertexBuffer.position() < vertexCount * VERTEX_DATA_SIZE) {
            FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            floatBuffer.put(vertexBuffer.flip());
            vertexBuffer = floatBuffer;
        }

        while (cmdBuffer.capacity() - cmdBuffer.position() < COMMAND_SIZE_IN_BYTES) {
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(cmdBuffer.capacity() << 1);
            byteBuffer.put(cmdBuffer.flip());
            cmdBuffer = byteBuffer;
        }
    }

    public void clear() {
        vertexBuffer.clear();
        cmdBuffer.clear();
        objectsCount = 0;
    }
}