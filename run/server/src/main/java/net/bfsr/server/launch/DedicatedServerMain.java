package net.bfsr.server.launch;

import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.DedicatedServer;
import net.bfsr.server.DedicatedServerGameLogic;

public final class DedicatedServerMain {
    public static void main(String[] args) {
        new DedicatedServer(new DedicatedServerGameLogic(new Profiler())).run();
    }
}