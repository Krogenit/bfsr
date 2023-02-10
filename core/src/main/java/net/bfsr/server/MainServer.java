package net.bfsr.server;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.core.Core;
import net.bfsr.core.Loop;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.EnumGui;
import net.bfsr.network.NetworkSystem;
import net.bfsr.network.packet.server.PacketJoinGame;
import net.bfsr.network.packet.server.PacketOpenGui;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.network.status.ServerStatusResponse;
import net.bfsr.profiler.Profiler;
import net.bfsr.settings.ServerSettings;
import net.bfsr.world.WorldServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class MainServer extends Loop {
    @Getter
    private static MainServer instance;

    @Getter
    private final boolean singlePlayer;
    @Getter
    private int ups;

    @Getter
    private WorldServer world;
    @Getter
    private SimpleDataBase dataBase;
    @Getter
    private NetworkSystem networkSystem;
    @Getter
    private Profiler profiler;
    private ServerSettings settings;
    private PlayerManager playerManager;

    public MainServer(boolean singlePlayer) {
        instance = this;
        this.singlePlayer = singlePlayer;
        if (singlePlayer) {
            log.info("Starting local server...");
        } else {
            log.info("Starting dedicated server...");
        }
    }

    @Override
    public void run() {
        log.info("Initialization server...");
        init();
        log.info("Initialized");
        super.run();
        loop();
    }

    private void init() {
        networkSystem = new NetworkSystem(this);
        profiler = new Profiler();
        profiler.setEnable(true);
        dataBase = new SimpleDataBase(this);
        world = new WorldServer(profiler);
        playerManager = new PlayerManager(world);
//		world.spawnShips();

        if (!singlePlayer) {
            settings = new ServerSettings();
            settings.readSettings();
            InetAddress inetaddress;
            try {
                inetaddress = InetAddress.getByName(settings.getHostName());
                networkSystem.addLanEndpoint(inetaddress, settings.getPort());
                log.info("Set server address {}:{}", settings.getHostName(), settings.getPort());
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Can't start server on address " + settings.getHostName() + ":" + settings.getPort(), e);
            }

            Thread t = new Thread(() -> {
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
        }
    }

    public void initializeConnectionToPlayer(NetworkManagerServer networkManager, PlayerServer player) {
        player.setNetworkManager(networkManager);
        world.addNewPlayer(player);
        networkManager.scheduleOutboundPacket(new PacketJoinGame(world.getSeed()));
        if (player.getFaction() != null) {
            dataBase.getPlayerShips(player);
            if (player.getShips().isEmpty()) {
                playerManager.respawnPlayer(player, 0, 0);
            }
        } else {
            networkSystem.sendPacketTo(new PacketOpenGui(EnumGui.SelectFaction), player);
        }
    }

    @Override
    protected void update() {
        profiler.startSection("update");
        if (!singlePlayer || !Core.get().isPaused()) world.update();
        profiler.endStartSection("network");
        networkSystem.networkTick();
        profiler.endSection();
    }

    @Override
    protected void setFps(int fps) {
        ups = fps;
    }

    @Override
    protected void clear() {
        log.info("Terminating network...");
        if (networkSystem != null && !singlePlayer) {
            networkSystem.terminateEndpoints();
        }

        log.info("Save base data...");
        dataBase.save();
        log.info("Clearing world...");
        world.clear();
        world = null;
        log.info("Stopped");
    }

    public void stop() {
        log.info("Stopping server...");
        super.stop();
    }

    public ServerStatusResponse getStatus() {
        return new ServerStatusResponse(10, "0.0.4");
    }

    public static void main(String[] args) {
        new MainServer(false).run();
    }
}
