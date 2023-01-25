package net.bfsr.client.render;

import net.bfsr.client.render.primitive.DynamicVertexColorBuffer;
import net.bfsr.client.render.primitive.VertexColorBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class RenderUtils {
    private static final VAO VERTEX_COLOR_VAO = VAO.create(2);

    static {
        VERTEX_COLOR_VAO.createVertexBuffers();
        VERTEX_COLOR_VAO.enableAttributes(2);
    }

    public static void render(int drawType, VertexColorBuffer vertexColorBuffer) {
        VERTEX_COLOR_VAO.bind();
        VERTEX_COLOR_VAO.updateBuffer(0, vertexColorBuffer.getVertexBuffer(), GL15.GL_DYNAMIC_DRAW);
        VERTEX_COLOR_VAO.updateBuffer(1, vertexColorBuffer.getColorBuffer(), GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(drawType, 0, vertexColorBuffer.getVertexCount());
    }

    public static void render(int drawType, DynamicVertexColorBuffer vertexColorBuffer) {
        VERTEX_COLOR_VAO.bind();
        VERTEX_COLOR_VAO.updateBuffer(0, vertexColorBuffer.getVertexBuffer(), GL15.GL_DYNAMIC_DRAW);
        VERTEX_COLOR_VAO.updateBuffer(1, vertexColorBuffer.getColorBuffer(), GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(drawType, 0, vertexColorBuffer.getVertexCount());
    }
}
