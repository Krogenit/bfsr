package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class FontShaderSpecial extends Program {
    private int loc_orthographicMatrix;
    private int loc_modeViewMatrix;

    public FontShaderSpecial() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "font_special.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "font_special.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_orthographicMatrix = super.getUniformLocation("orthographicMatrix");
        loc_modeViewMatrix = super.getUniformLocation("modelViewMatrix");
    }

    @Override
    protected void initUniforms() {

    }

    public void setOrthographicMatrix(Matrix4f matrix) {
        super.setMat4(loc_orthographicMatrix, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        super.setMat4(loc_modeViewMatrix, matrix);
    }
}
