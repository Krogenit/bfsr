package net.bfsr.engine.renderer.particle;

public enum ParticleType {
    ALPHA_BLENDED, ADDITIVE;

    static final ParticleType[] VALUES = values();

    public static ParticleType get(byte index) {
        return VALUES[index];
    }
}