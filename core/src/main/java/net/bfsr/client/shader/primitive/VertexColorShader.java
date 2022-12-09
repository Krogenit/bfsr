package net.bfsr.client.shader.primitive;

import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.client.shader.loader.Definition;
import net.bfsr.core.Core;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class VertexColorShader extends ShaderProgram {
    public VertexColorShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "vertex_color.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "vertex_color.frag.glsl"));
    }

    @Override
    public void setModelMatrix(Matrix4f matrix) {
        Core.getCore().getRenderer().setModelMatrix(matrix);
    }

    @Override
    protected void getAllUniformLocations() {

    }

    @Override
    protected void initUniforms() {

    }
}
