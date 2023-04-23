package net.bfsr.server.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.component.shield.ShieldRegistry;
import net.bfsr.config.bullet.BulletRegistry;
import net.bfsr.config.weapon.beam.BeamRegistry;
import net.bfsr.config.weapon.gun.GunRegistry;
import net.bfsr.core.Loop;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.server.rsocket.RSocketClient;
import net.bfsr.server.service.PlayerService;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.PathHelper;

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
    private final PlayerService playerService;
    @Getter
    private final NetworkSystem networkSystem;
    @Getter
    private final Profiler profiler = new Profiler();
    private ServerSettings settings;
    @Getter
    private final PlayerManager playerManager;
    @Getter
    protected boolean pause;
    @Getter
    private final RSocketClient databaseRSocketClient = new RSocketClient();

    protected Server() {
        this.networkSystem = new NetworkSystem(this);
        this.world = new WorldServer(profiler);
        this.playerService = new PlayerService(databaseRSocketClient);
        this.playerManager = new PlayerManager(world);

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
        WreckRegistry.INSTANCE.init(PathHelper.CONFIG);
        ShieldRegistry.INSTANCE.init(PathHelper.CONFIG);
        BulletRegistry.INSTANCE.init();
        GunRegistry.INSTANCE.init();
        BeamRegistry.INSTANCE.init();
        settings = createSettings();
        startupNetworkSystem(settings);
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

        databaseRSocketClient.connect(serverSettings.getDataBaseServiceHost(), serverSettings.getDatabaseServicePort());
    }

    @Override
    protected void update() {
        profiler.startSection("update");
        updateWorld();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endSection();
    }

    protected void updateWorld() {
        world.update();
    }

    public void onPlayerDisconnected(Player player) {
        world.removePlayer(player);
        playerService.removePlayer(player.getUsername());
        playerService.save(player);
        List<Ship> ships = player.getShips();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship s = ships.get(i);
            s.setOwner(null);
            s.setDead(true);
            networkSystem.sendUDPPacketToAllNearby(new PacketRemoveObject(s), s.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    @Override
    protected void setFps(int fps) {
        ups = fps;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    @Override
    protected void clear() {
        super.clear();
        log.info("Terminating network...");
        networkSystem.shutdown();
        databaseRSocketClient.clear();
        log.info("Saving database...");
        playerService.save();
        log.info("Clearing world...");
        world.clear();
        log.info("Stopped");
    }

    public void stop() {
        log.info("Stopping server...");
        super.stop();
    }
}