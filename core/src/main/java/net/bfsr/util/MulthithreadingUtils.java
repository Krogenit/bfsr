package net.bfsr.util;

public class MulthithreadingUtils {
    public static final int PARALLELISM = Runtime.getRuntime().availableProcessors();
    public static final boolean MULTITHREADING_SUPPORTED = PARALLELISM > 1;
}
