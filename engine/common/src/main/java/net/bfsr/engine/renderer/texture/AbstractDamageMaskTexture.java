package net.bfsr.engine.renderer.texture;

public abstract class AbstractDamageMaskTexture {
    public abstract float getFireAmount();
    public abstract float getFireUVAnimation();
    public abstract long getTextureHandle();
    public abstract void delete();
}