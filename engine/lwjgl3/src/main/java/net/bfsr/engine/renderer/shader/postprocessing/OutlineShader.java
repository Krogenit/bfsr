package net.bfsr.engine.renderer.shader.postprocessing;

import net.bfsr.engine.renderer.shader.ShaderProgram;
import net.bfsr.engine.renderer.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class OutlineShader extends ShaderProgram {
    private int loc_offset;

    public OutlineShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "postprocessing/outline.vert.glsl"),
                new Definition(GL20.GL_FRAGMENT_SHADER, "postprocessing/outline.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_offset = getUniformLocation("offset");
    }

    public void setOffset(float offset) {
        setFloat(loc_offset, offset);
    }
}