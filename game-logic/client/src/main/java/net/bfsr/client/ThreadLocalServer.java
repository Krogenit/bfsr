package net.bfsr.client;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ThreadLocalServer extends Thread {
    private final LocalServer server;

    @Override
    public void run() {
        server.run();
    }
}