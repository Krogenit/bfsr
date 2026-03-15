package net.bfsr.engine.renderer;

import net.bfsr.engine.Engine;

public final class DepthBufferRenderLayers {
    private static final int DEPTH_BITS = Engine.getRenderer().getDepthBits();
    private static final int MAX_VALUE = 1 << DEPTH_BITS;
    private static final int MAX_LAYER_INDEX = MAX_VALUE - 1;
    private static final float MAX_LAYER_INDEX_FLOAT = (float) MAX_LAYER_INDEX;

    public static float getZ(int layerIndex) {
        return -1.0f + (layerIndex / MAX_LAYER_INDEX_FLOAT) * 2.0f;
    }

    public static float getUIZ() {
        return 1.0f;
    }

    public static float closeZ(float z) {
        return z + 1.0f / MAX_LAYER_INDEX_FLOAT * 2.0f;
    }

    public static float farZ(float z) {
        return z - 1.0f / MAX_LAYER_INDEX_FLOAT * 2.0f;
    }
}
