package net.bfsr.engine.renderer.texture;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.engine.renderer.AbstractRenderer;

public abstract class AbstractTextureGenerator {
    public abstract AbstractTexture generateNebulaTexture(int width, int height, XoRoShiRo128PlusRandom random, AbstractRenderer renderer);
}