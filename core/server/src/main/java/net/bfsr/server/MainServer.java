package net.bfsr.server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.component.shield.ShieldRegistry;
import net.bfsr.core.Loop;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.network.EnumGui;
import net.bfsr.network.status.ServerStatusResponse;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.database.SimpleDataBase;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.packet.server.PacketJoinGame;
import net.bfsr.server.network.packet.server.PacketOpenGui;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.util.PathHelper;
import net.bfsr.server.world.WorldServer;

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
    @Getter
    @Setter
    private boolean pause;

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
        WreckRegistry.INSTANCE.init(PathHelper.CONFIG);
        ShieldRegistry.INSTANCE.init(PathHelper.CONFIG);
        dataBase = new SimpleDataBase(this);
        world = new WorldServer(profiler);
        playerManager = new PlayerManager(world);
//		world.spawnShips();

        String hostname;
        int port;
        if (!singlePlayer) {
            settings = new ServerSettings();
            settings.readSettings();
            hostname = settings.getHostName();
            port = settings.getPort();

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
        } else {
            hostname = "127.0.0.1";
            port = 34000;
        }

        InetAddress inetaddress;
        try {
            inetaddress = InetAddress.getByName(hostname);
            networkSystem.addLanEndpoint(inetaddress, port);
            log.info("Set server address {}:{}", hostname, port);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Can't start server on address " + hostname + ":" + port, e);
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
        if (!singlePlayer || !pause) world.update();
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
