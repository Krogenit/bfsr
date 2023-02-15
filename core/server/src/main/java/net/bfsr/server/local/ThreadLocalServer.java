package net.bfsr.server.local;

import lombok.Getter;
import net.bfsr.server.MainServer;

@Getter
public class ThreadLocalServer extends Thread {
    private final MainServer server;

    public ThreadLocalServer() {
        server = new MainServer(true);
    }

    @Override
    public void run() {
        server.run();
    }

    public void stopServer() {
        server.stop();
    }

    public boolean isRunning() {
        return server.isRunning();
    }
}
