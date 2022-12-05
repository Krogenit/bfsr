package net.bfsr.client.render;

import net.bfsr.client.render.primitive.DynamicVertexColorBuffer;
import net.bfsr.client.render.primitive.VertexColorBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class RenderUtils {
    private static final VAO VERTEX_COLOR_VAO = VAO.create(2);

    static {
        VERTEX_COLOR_VAO.createAttribute(0, 2, 0);
        VERTEX_COLOR_VAO.createAttribute(1, 4, 1);
        VERTEX_COLOR_VAO.bindAttribs();
    }

    public static void render(int drawType, VertexColorBuffer vertexColorBuffer) {
        VERTEX_COLOR_VAO.bind();
        VERTEX_COLOR_VAO.updateAttribute(0, vertexColorBuffer.getVertexBuffer(), GL15.GL_DYNAMIC_DRAW);
        VERTEX_COLOR_VAO.updateAttribute(1, vertexColorBuffer.getColorBuffer(), GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(drawType, 0, vertexColorBuffer.getVertexCount());
    }

    public static void render(int drawType, DynamicVertexColorBuffer vertexColorBuffer) {
        VERTEX_COLOR_VAO.bind();
        VERTEX_COLOR_VAO.updateAttribute(0, vertexColorBuffer.getVertexBuffer(), GL15.GL_DYNAMIC_DRAW);
        VERTEX_COLOR_VAO.updateAttribute(1, vertexColorBuffer.getColorBuffer(), GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(drawType, 0, vertexColorBuffer.getVertexCount());
    }
}
