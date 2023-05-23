package net.bfsr.server.core;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.config.ServerSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Log4j2
public class DedicatedServer extends Server {
    @Override
    public void run() {
        log.info("Starting dedicated server...");
        super.run();
    }

    @Override
    protected void init() {
        super.init();
        startConsoleInputThread();
    }

    @Override
    protected ServerSettings createSettings() {
        return ServerSettings.load();
    }

    private void startConsoleInputThread() {
        Thread t = new Thread(() -> {
            while (!isRunning()) {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            while (isRunning()) {
                String name;
                try {
                    name = reader.readLine();
                    if ("stop".equals(name)) {
                        stop();
                    }
                } catch (IOException e) {
                    log.error("Can't read line from console input", e);
                }
            }
        });
        t.setName("Console Input");
        t.start();
    }

    @Override
    public void setPause(boolean pause) {}
}