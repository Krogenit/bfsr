package net.bfsr.server.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.component.shield.ShieldRegistry;
import net.bfsr.core.Loop;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.PlayerManager;
import net.bfsr.server.ServerSettings;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.service.PlayerService;
import net.bfsr.server.util.PathHelper;
import net.bfsr.server.world.WorldServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class Server extends Loop {
    @Getter
    private static Server instance;

    @Getter
    @Setter
    private boolean local;
    @Getter
    private int ups;

    @Getter
    private final WorldServer world;
    @Getter
    private final PlayerService playerService;
    @Getter
    private final NetworkSystem networkSystem;
    @Getter
    private final Profiler profiler = new Profiler();
    private ServerSettings settings;
    @Getter
    private final PlayerManager playerManager;
    @Getter
    @Setter
    private boolean pause;

    public Server() {
        this.networkSystem = new NetworkSystem(this);
        this.world = new WorldServer(profiler);
        this.playerService = new PlayerService();
        this.playerManager = new PlayerManager(world);

        instance = this;
    }

    @Override
    public void run() {
        if (local) {
            log.info("Starting local server...");
        } else {
            log.info("Starting dedicated server...");
        }

        log.info("Server initialization...");
        init();
        log.info("Initialized");
        super.run();
        loop();
    }

    private void init() {
        profiler.setEnable(true);
        networkSystem.init();
        WreckRegistry.INSTANCE.init(PathHelper.CONFIG);
        ShieldRegistry.INSTANCE.init(PathHelper.CONFIG);
//		world.spawnShips();

        String hostname;
        int port;
        if (!local) {
            settings = new ServerSettings();
            settings.readSettings();
            hostname = settings.getHostName();
            port = settings.getPort();

            Thread t = new Thread(() -> {
                while (!isRunning()) {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                while (isRunning()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
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
        } else {
            hostname = "127.0.0.1";
            port = 34000;
        }

        InetAddress inetaddress;
        try {
            inetaddress = InetAddress.getByName(hostname);
            networkSystem.startup(inetaddress, port);
            log.info("Set server address {}:{}", hostname, port);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Can't start server on address " + hostname + ":" + port, e);
        }
    }

    @Override
    protected void update() {
        profiler.startSection("update");
        if (!local || !pause) world.update();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endSection();
    }

    @Override
    protected void setFps(int fps) {
        ups = fps;
    }

    @Override
    protected void clear() {
        super.clear();
        log.info("Terminating network...");
        networkSystem.shutdown();
        log.info("Saving database...");
        playerService.save();
        log.info("Clearing world...");
        world.clear();
        log.info("Stopping spring boot...");
        SpringContext.stop();
        log.info("Clearing spring context...");
        SpringContext.clear();
        log.info("Stopped");
    }

    public void stop() {
        log.info("Stopping server...");
        super.stop();
    }
}