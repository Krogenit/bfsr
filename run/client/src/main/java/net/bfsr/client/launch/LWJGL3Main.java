package net.bfsr.client.launch;

import net.bfsr.client.Core;
import net.bfsr.engine.LWJGL3Engine;

public final class LWJGL3Main {
    public static void main(String[] args) {
        new LWJGL3Engine(Core.class).run();
    }
}