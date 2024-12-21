package net.bfsr.client.launch;

import net.bfsr.benchmark.BenchmarkGameLogic;
import net.bfsr.engine.LWJGL3Engine;

public final class BenchmarkMain {
    public static void main(String[] args) {
        new LWJGL3Engine(BenchmarkGameLogic.class).run();
    }
}