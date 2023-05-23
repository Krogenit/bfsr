package net.bfsr.server.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.engine.loop.Loop;
import net.bfsr.entity.ship.Ship;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.event.listener.Listeners;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.server.world.WorldServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Log4j2
public abstract class Server extends Loop {
    @Getter
    private static Server instance;

    @Getter
    private int ups;

    @Getter
    private final WorldServer world;
    @Getter
    private final NetworkSystem networkSystem;
    @Getter
    private final Profiler profiler = new Profiler();
    private ServerSettings settings;
    @Getter
    private final PlayerManager playerManager;
    @Getter
    protected boolean pause;

    protected Server() {
        this.world = new WorldServer(profiler);
        this.playerManager = new PlayerManager(world);
        this.networkSystem = new NetworkSystem(playerManager);

        instance = this;
    }

    @Override
    public void run() {
        log.info("Server initialization...");
        init();
        log.info("Initialized");
        super.run();
        loop();
    }

    protected void init() {
        profiler.setEnable(true);
        networkSystem.init();
        loadConfigs();
        settings = createSettings();
        startupNetworkSystem(settings);
        Listeners.init();
    }

    protected void loadConfigs() {
        ConfigConverterManager.INSTANCE.init();
    }

    protected ServerSettings createSettings() {
        return new ServerSettings();
    }

    protected void startupNetworkSystem(ServerSettings serverSettings) {
        InetAddress inetaddress;
        try {
            inetaddress = InetAddress.getByName(serverSettings.getHostName());
            networkSystem.startup(inetaddress, serverSettings.getPort());
            log.info("Set server address {}:{}", serverSettings.getHostName(), serverSettings.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Can't start server on address " + serverSettings.getHostName() + ":" + serverSettings.getPort(), e);
        }

        playerManager.connect(serverSettings.getDataBaseServiceHost(), serverSettings.getDatabaseServicePort());
    }

    @Override
    protected void update() {
        profiler.startSection("playerManager");
        playerManager.update();
        profiler.endStartSection("update");
        updateWorld();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endSection();
    }

    protected void updateWorld() {
        world.update();
    }

    public void onPlayerDisconnected(Player player) {
        playerManager.removePlayer(player);
        playerManager.save(player);
        List<Ship> ships = player.getShips();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship s = ships.get(i);
            s.setOwner(null);
            s.setDead();
            networkSystem.sendTCPPacketToAll(new PacketRemoveObject(s));
        }
    }

    @Override
    protected void setFps(int fps) {
        ups = fps;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public static NetworkSystem getNetwork() {
        return instance.networkSystem;
    }

    @Override
    protected void clear() {
        super.clear();
        log.info("Saving database...");
        playerManager.saveAllSync();
        log.info("Clearing world...");
        world.clear();
        log.info("Terminating network...");
        networkSystem.shutdown();
        playerManager.clear();
        log.info("Stopped");
    }

    public void stop() {
        log.info("Stopping server...");
        super.stop();
    }
}