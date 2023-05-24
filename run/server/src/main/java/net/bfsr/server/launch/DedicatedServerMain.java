package net.bfsr.server.launch;

import net.bfsr.engine.loop.AbstractLoop;
import net.bfsr.server.DedicatedServer;
import net.bfsr.server.DedicatedServerGameLogic;

public final class DedicatedServerMain extends AbstractLoop {
    public static void main(String[] args) {
        new DedicatedServer(new DedicatedServerGameLogic()).run();
    }
}