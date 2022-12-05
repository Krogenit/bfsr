package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

@Deprecated
public class FontShader extends Program {
    private int location_color;
    private int location_translation;
    private int loc_orthoMat;
    private int loc_modelViewMat;

    public FontShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "font.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "font.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        location_color = getUniformLocation("color");
        location_translation = getUniformLocation("translation");
        loc_orthoMat = getUniformLocation("orthoMat");
        loc_modelViewMat = getUniformLocation("modelViewMat");
    }

    @Override
    protected void initUniforms() {
        setColor(new Vector4f(1f, 1f, 1f, 1f));
    }

    public void setColor(Vector4f colour) {
        setVector(location_color, colour);
    }

    public void loadTranslation(Vector2f translation) {
        setVector(location_translation, translation);
    }

    public void setOrthoMatrix(Matrix4f matrix) {
        setMat4(loc_orthoMat, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        setMat4(loc_modelViewMat, matrix);
    }
}
