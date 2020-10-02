package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;

public class FontShaderTextured extends ShaderProgram {

    private int loc_orthographicMatrix;
    private int loc_modeViewMatrix;

    public FontShaderTextured() {
        super("font_textured");
    }

    @Override
    protected void getAllUniformLocations() {
        loc_orthographicMatrix = super.getUniformLocation("orthographicMatrix");
        loc_modeViewMatrix = super.getUniformLocation("modelViewMatrix");
    }

    @Override
    protected void init() {

    }

    public void setOrthographicMatrix(Matrix4f matrix) {
        super.setMat4(loc_orthographicMatrix, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        super.setMat4(loc_modeViewMatrix, matrix);
    }
}
