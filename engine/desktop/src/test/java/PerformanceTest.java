public final class PerformanceTest {
    private static long used;
    private static long startTime;

    public static void beginTest() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        used = total - free;
        startTime = System.nanoTime();
    }

    public static void finishTest(String testName) {
        outInfo(testName, used, startTime);
    }

    public static long outInfo(String name, long used, long startTime) {
        System.out.println(name + " " + ((System.nanoTime() - startTime) / 1_000_000f) + " ms");
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long testUsed = total - free - used;
        System.out.println("Used: " + testUsed / 1024L + "KB");
        System.out.println();
        return total - free;
    }
}