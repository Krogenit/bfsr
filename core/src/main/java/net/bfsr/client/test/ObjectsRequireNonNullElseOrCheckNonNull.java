package net.bfsr.client.test;

import java.util.Objects;
import java.util.Random;

/**
 * Objects faster
 */
public class ObjectsRequireNonNullElseOrCheckNonNull extends PerformanceTest {
    public static void main(String[] args) {
        Random random = new Random();
        int count = 1_000_000;
        Integer[] integers = new Integer[count];

        for (int k = 0; k < 10; k++) {
            for (int i = 0; i < count; i++) {
                integers[i] = random.nextBoolean() ? random.nextInt() : null;
            }

            PerformanceTest.beginTest();
            Integer value = null;
            for (int i = 0; i < count; i++) {
                value = Objects.requireNonNullElse(integers[i], -1);
            }
            PerformanceTest.finishTest("Objects");
            System.out.println(value);
            PerformanceTest.beginTest();
            for (int i = 0; i < count; i++) {
                value = integers[i] != null ? integers[i] : -1;
            }
            PerformanceTest.finishTest("Forward check");
            System.out.println(value);
        }
    }
}
