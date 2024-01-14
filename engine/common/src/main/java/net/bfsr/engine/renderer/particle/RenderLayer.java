package net.bfsr.engine.renderer.particle;

public enum RenderLayer {
    BACKGROUND_ALPHA_BLENDED, BACKGROUND_ADDITIVE, DEFAULT_ALPHA_BLENDED, DEFAULT_ADDITIVE;

    static final RenderLayer[] VALUES = values();

    public static RenderLayer get(byte index) {
        return VALUES[index];
    }
}