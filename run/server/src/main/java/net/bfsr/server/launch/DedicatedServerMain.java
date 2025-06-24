package net.bfsr.server.launch;

import net.bfsr.server.dedicated.DedicatedServer;
import net.bfsr.server.dedicated.DedicatedServerGameLogic;

public final class DedicatedServerMain {
    public static void main(String[] args) {
        new DedicatedServer(DedicatedServerGameLogic.class).run();
    }
}