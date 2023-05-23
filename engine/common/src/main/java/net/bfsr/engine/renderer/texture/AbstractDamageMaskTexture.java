package net.bfsr.engine.renderer.texture;

public abstract class AbstractDamageMaskTexture {
    public abstract float getFireAmount(float interpolation);
    public abstract float getFireUVAnimation(float interpolation);
    public abstract long getTextureHandle();
    public abstract void delete();
}