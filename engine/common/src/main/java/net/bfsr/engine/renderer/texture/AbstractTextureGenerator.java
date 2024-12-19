package net.bfsr.engine.renderer.texture;

import java.util.Random;

public abstract class AbstractTextureGenerator {
    public abstract AbstractTexture generateNebulaTexture(int width, int height, Random random);
}