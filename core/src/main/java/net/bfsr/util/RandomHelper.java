package net.bfsr.util;

import java.util.Random;

public class RandomHelper {
    public static float randomFloat(Random r, float min, float max) {
        return min + r.nextFloat() * (max - min);
    }
}
