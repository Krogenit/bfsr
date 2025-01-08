package net.bfsr.engine.util;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public final class RandomHelper {
    public static float randomFloat(XoRoShiRo128PlusRandom random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}