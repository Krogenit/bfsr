package net.bfsr.engine.renderer.shader.postprocessing;

import net.bfsr.engine.renderer.shader.ShaderProgram;
import net.bfsr.engine.renderer.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class GaussianBlurShader extends ShaderProgram {
    private int loc_resolution;
    private int loc_size;

    public GaussianBlurShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "postprocessing/gaussian-blur.vert.glsl"),
                new Definition(GL20.GL_FRAGMENT_SHADER, "postprocessing/gaussian-blur.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_resolution = getUniformLocation("resolution");
        loc_size = getUniformLocation("size");
    }

    public void setResolution(float width, float height) {
        setVector(loc_resolution, width, height);
    }

    public void setSize(float size) {
        setFloat(loc_size, size);
    }
}
