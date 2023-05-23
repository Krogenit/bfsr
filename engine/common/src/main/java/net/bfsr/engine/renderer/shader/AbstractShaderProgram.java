package net.bfsr.engine.renderer.shader;

public abstract class AbstractShaderProgram {
    public abstract void load();
    public abstract void init();
    public abstract void enable();
    public abstract void delete();
}