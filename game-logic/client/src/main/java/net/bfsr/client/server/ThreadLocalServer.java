package net.bfsr.client.server;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ThreadLocalServer extends Thread {
    private final LocalServer server;

    @Override
    public void run() {
        server.run();
    }
}