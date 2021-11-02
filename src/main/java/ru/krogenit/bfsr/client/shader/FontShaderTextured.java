package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;

public class FontShaderTextured extends ShaderProgram {
    private int loc_projection;
    private int loc_modeView;

    public FontShaderTextured() {
        super("font_textured");
    }

    @Override
    protected void getAllUniformLocations() {
        loc_projection = getUniformLocation("projection");
        loc_modeView = getUniformLocation("modelView");
    }

    @Override
    protected void init() {

    }

    public void setOrthographicMatrix(Matrix4f matrix) {
        setMat4(loc_projection, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        setMat4(loc_modeView, matrix);
    }
}
