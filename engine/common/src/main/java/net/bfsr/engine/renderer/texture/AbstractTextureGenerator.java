package net.bfsr.engine.renderer.texture;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public abstract class AbstractTextureGenerator {
    public abstract AbstractTexture generateNebulaTexture(int width, int height, XoRoShiRo128PlusRandom random);
}