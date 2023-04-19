package net.bfsr.server.local;

import net.bfsr.server.LocalServerSpringApplication;
import net.bfsr.server.core.Server;
import net.bfsr.server.core.SpringContext;

public class ThreadLocalServer extends Thread {
    @Override
    public void run() {
        LocalServerSpringApplication.main();
    }

    public Server getServer() {
        return SpringContext.isAvailable() ? SpringContext.getBean(Server.class) : null;
    }
}