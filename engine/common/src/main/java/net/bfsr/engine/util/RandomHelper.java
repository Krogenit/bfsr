package net.bfsr.engine.util;

import java.util.Random;

public final class RandomHelper {
    public static float randomFloat(Random r, float min, float max) {
        return min + r.nextFloat() * (max - min);
    }
}