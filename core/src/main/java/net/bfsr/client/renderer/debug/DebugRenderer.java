package net.bfsr.client.renderer.debug;

import net.bfsr.client.renderer.RenderUtils;
import net.bfsr.client.renderer.primitive.DynamicVertexColorBuffer;
import net.bfsr.client.renderer.primitive.VertexColorBuffer;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Wound;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class DebugRenderer {
    public static final DebugRenderer INSTANCE = new DebugRenderer();

    private final VertexColorBuffer vertexColorBuffer = new VertexColorBuffer(4);
    private final DynamicVertexColorBuffer dynamicVertexColorBuffer = new DynamicVertexColorBuffer(4);
    private final Matrix4f modelMatrix = new Matrix4f();

    public void renderAABB(AABB aabb) {
        vertexColorBuffer.addVertex((float) aabb.getMinX(), (float) aabb.getMinY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMinX(), (float) aabb.getMaxY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMaxX(), (float) aabb.getMaxY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.addVertex((float) aabb.getMaxX(), (float) aabb.getMinY(), 1.0f, 1.0f, 1.0f, 1.0f);
        vertexColorBuffer.flip();
        RenderUtils.render(GL11.GL_LINE_LOOP, vertexColorBuffer);
    }

    public void renderWound(Wound wound) {
        int length = wound.getVertices().length;
        dynamicVertexColorBuffer.reset();
        for (int i = 0; i < length; i++) {
            Vector2 vector = wound.getVertices()[i];
            dynamicVertexColorBuffer.addVertex((float) vector.x, (float) vector.y, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        dynamicVertexColorBuffer.flip();
        RenderUtils.render(GL11.GL_LINE_LOOP, dynamicVertexColorBuffer);
    }
}
