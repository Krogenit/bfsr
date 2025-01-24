package net.bfsr.server.launch;

import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.dedicated.DedicatedServer;
import net.bfsr.server.dedicated.DedicatedServerGameLogic;

public final class DedicatedServerMain {
    public static void main(String[] args) {
        new DedicatedServer(new DedicatedServerGameLogic(new Profiler(), new EventBus())).run();
    }
}