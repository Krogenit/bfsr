package net.bfsr.client.launch;

import net.bfsr.client.Client;
import net.bfsr.engine.LWJGL3Engine;

public final class LWJGL3Main {
    public static void main(String[] args) {
        new LWJGL3Engine(Client.class).run();
    }
}