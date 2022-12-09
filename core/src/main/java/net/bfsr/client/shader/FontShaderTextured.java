package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class FontShaderTextured extends ShaderProgram {
    private int loc_projection;
    private int loc_modeView;

    public FontShaderTextured() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "font_textured.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "font_textured.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_projection = getUniformLocation("projection");
        loc_modeView = getUniformLocation("modelView");
    }

    @Override
    protected void initUniforms() {

    }

    public void setOrthographicMatrix(Matrix4f matrix) {
        setMat4(loc_projection, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        setMat4(loc_modeView, matrix);
    }
}
