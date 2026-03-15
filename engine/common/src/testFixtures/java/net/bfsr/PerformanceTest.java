package net.bfsr;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PerformanceTest {
    private static long used;
    private static long startTime;
    private static final Map<String, Float> averageTestTime = new LinkedHashMap<>();
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.000");

    public static void beginTest() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        used = total - free;
        startTime = System.nanoTime();
    }

    public static float finishTest(String testName) {
        return outInfo(testName, used, startTime);
    }

    public static float outInfo(String name, long used, long startTime) {
        float timeConsumed = (System.nanoTime() - startTime) / 1_000_000f;
        System.out.println(name + " " + timeConsumed + " ms");
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long testUsed = total - free - used;
        System.out.println("Used: " + testUsed / 1024L + "KB");
        System.out.println();

        if (averageTestTime.containsKey(name)) {
            averageTestTime.put(name, (averageTestTime.get(name) + timeConsumed) / 2);
        } else {
            averageTestTime.put(name, timeConsumed);
        }

        return timeConsumed;
    }

    public static void outAverageTimeInfo() {
        ArrayList<Map.Entry<String, Float>> objects = new ArrayList<>(averageTestTime.entrySet());
        objects.sort(Map.Entry.comparingByValue());

        for (int i = 0; i < objects.size(); i++) {
            Map.Entry<String, Float> entry = objects.get(i);
            System.out.println("Average time for test " + entry.getKey() + ": " + decimalFormat.format(entry.getValue()) + "ms");
        }
    }
}