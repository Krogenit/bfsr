package net.bfsr.engine.renderer.debug;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.primitive.VAO;
import net.bfsr.engine.renderer.shader.DebugShader;
import net.bfsr.engine.util.MutableInt;
import org.jbox2d.collision.AABB;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FOUR_BYTES_ELEMENT_SHIFT;
import static org.lwjgl.opengl.GL43C.glMultiDrawArraysIndirect;

public class DebugRenderer implements AbstractDebugRenderer {
    private static final int VERTEX_DATA_SIZE = 6;
    private static final int VERTEX_DATA_SIZE_IN_BYTES = VERTEX_DATA_SIZE << 2;
    private static final int COMMAND_SIZE_IN_BYTES = 4;
    private static final int VERTEX_BUFFER_RESIZE_CAPACITY = 64;
    private static final int COMMAND_BUFFER_RESIZE_CAPACITY = 32;

    private final DebugShader debugShader = new DebugShader();
    private VAO vao;
    private FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(VERTEX_BUFFER_RESIZE_CAPACITY);
    private IntBuffer cmdBuffer = MemoryUtil.memAllocInt(COMMAND_BUFFER_RESIZE_CAPACITY);
    private final MutableInt vertexBufferIndex = new MutableInt();
    private final MutableInt cmdBufferIndex = new MutableInt();
    private long vertexBufferAddress = MemoryUtil.memAddress(vertexBuffer);
    private long cmdBufferAddress = MemoryUtil.memAddress(cmdBuffer);
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
    public void renderAABB(AABB aabb, Vector4f color) {
        addCommand(4);
        addVertex(aabb.getMinX(), aabb.getMinY(), color);
        addVertex(aabb.getMinX(), aabb.getMaxY(), color);
        addVertex(aabb.getMaxX(), aabb.getMaxY(), color);
        addVertex(aabb.getMaxX(), aabb.getMinY(), color);
    }

    @Override
    public void render(int mode) {
        if (objectsCount > 0) {
            debugShader.enable();
            vao.bind();
            vao.updateVertexBuffer(0, vertexBuffer.limit(vertexBufferIndex.get()), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE_IN_BYTES);
            vao.updateBuffer(1, cmdBuffer.limit(cmdBufferIndex.get()), GL44C.GL_DYNAMIC_STORAGE_BIT);
            vao.bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, 1);
            glMultiDrawArraysIndirect(mode, 0, objectsCount, 0);
            Engine.renderer.increaseDrawCalls();
        }
    }

    @Override
    public void addCommand(int count) {
        checkBuffersSize(count);
        putCommandData(count);
        putCommandData(1);// instance count
        putCommandData(vertexBufferIndex.get() / VERTEX_DATA_SIZE);// first vertex index
        putCommandData(objectsCount++);// base instance
    }

    @Override
    public void addVertex(float x, float y, Vector4f color) {
        putVertexData(x);
        putVertexData(y);
        putVertexData(color.x);
        putVertexData(color.y);
        putVertexData(color.z);
        putVertexData(color.w);
    }

    private void putCommandData(int value) {
        MemoryUtil.memPutInt(cmdBufferAddress + ((cmdBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT), value);
    }

    private void putVertexData(float value) {
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                value);
    }

    private void checkBuffersSize(int vertexCount) {
        int remainingBufferCapacity = vertexBuffer.capacity() - vertexBufferIndex.get();
        int requiredBufferCapacity = vertexCount * VERTEX_DATA_SIZE;
        if (remainingBufferCapacity < requiredBufferCapacity) {
            int resizeAmount = Math.max(VERTEX_BUFFER_RESIZE_CAPACITY, requiredBufferCapacity);
            vertexBuffer = MemoryUtil.memRealloc(vertexBuffer, vertexBuffer.capacity() + resizeAmount);
            vertexBufferAddress = MemoryUtil.memAddress(vertexBuffer);
        }

        if (cmdBuffer.capacity() - cmdBufferIndex.get() < COMMAND_SIZE_IN_BYTES) {
            int resizeAmount = Math.max(COMMAND_BUFFER_RESIZE_CAPACITY, COMMAND_SIZE_IN_BYTES);
            cmdBuffer = MemoryUtil.memRealloc(cmdBuffer, cmdBuffer.capacity() + resizeAmount);
            cmdBufferAddress = MemoryUtil.memAddress(cmdBuffer);
        }
    }

    @Override
    public void reload() {
        debugShader.delete();
        debugShader.load();
        debugShader.init();
    }

    public void reset() {
        objectsCount = 0;
        vertexBufferIndex.set(0);
        cmdBufferIndex.set(0);
    }

    @Override
    public void clear() {
        debugShader.delete();
        vao.clear();
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(cmdBuffer);
    }
}