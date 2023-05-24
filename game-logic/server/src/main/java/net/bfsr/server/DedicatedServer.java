package net.bfsr.server;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Log4j2
public class DedicatedServer extends Server {
    public DedicatedServer(ServerGameLogic gameLogic) {
        super(gameLogic);
    }

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
                        gameLogic.stop();
                    }
                } catch (IOException e) {
                    log.error("Can't read line from console input", e);
                }
            }
        });
        t.setName("Console Input");
        t.start();
    }
}