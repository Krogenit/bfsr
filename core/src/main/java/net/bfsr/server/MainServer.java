package net.bfsr.server;

import net.bfsr.core.Loop;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.log.LoggingSystem;
import net.bfsr.network.EnumGui;
import net.bfsr.network.NetworkSystem;
import net.bfsr.network.packet.server.PacketJoinGame;
import net.bfsr.network.packet.server.PacketOpenGui;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.network.status.ServerStatusResponse;
import net.bfsr.profiler.Profiler;
import net.bfsr.settings.ServerSettings;
import net.bfsr.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class MainServer extends Loop {

    static {
        LoggingSystem.initServer();
    }

    private static final Logger LOGGER = LogManager.getLogger(MainServer.class);
    private static MainServer instance;

    private boolean isRunning;
    private int tps;

    private WorldServer world;
    private SimpleDataBase dataBase;
    private NetworkSystem networkSystem;
    private Profiler profiler;
    private ServerSettings settings;
    private PlayerManager playerManager;

    private final boolean isSinglePlayer;

    public static void main(String[] args) {
        new MainServer(false).run();
    }

    public MainServer(boolean isSinglePlayer) {
        instance = this;
        this.isSinglePlayer = isSinglePlayer;
        if (isSinglePlayer) {
            LOGGER.log(Level.INFO, "Starting local server...");
        } else {
            LOGGER.log(Level.INFO, "Starting dedicated server...");
        }
    }

    public void run() {
        LOGGER.log(Level.INFO, "Initialization server...");
        init();
        LOGGER.log(Level.INFO, "Initialized");
        isRunning = true;
        LOGGER.log(Level.INFO, "Started");
        loop();
    }

    private void init() {
        networkSystem = new NetworkSystem(this);
        profiler = new Profiler(true);
        dataBase = new SimpleDataBase(this);
        world = new WorldServer(this, profiler);
        playerManager = new PlayerManager(this, world);
//		world.spawnShips();

        if (!isSinglePlayer) {
            settings = new ServerSettings();
            settings.readSettings();
            InetAddress inetaddress;
            try {
                inetaddress = InetAddress.getByName(settings.getHostName());
                networkSystem.addLanEndpoint(inetaddress, settings.getPort());
                LOGGER.log(Level.INFO, "Set server address " + settings.getHostName() + ":" + settings.getPort());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            Thread t = new Thread(() -> {
                while (!isRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while (isRunning) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String name;
                    try {
                        name = reader.readLine();
                        if (name.equals("stop")) {
                            isRunning = false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }); t.setName("Console Input"); t.start();
        }
    }

    public void initializeConnectionToPlayer(NetworkManagerServer networkManager, PlayerServer player) {
        player.setNetworkManager(networkManager);
        world.addNewPlayer(player);
        networkManager.scheduleOutboundPacket(new PacketJoinGame(world.getSeed()));
        if (player.getFaction() != null) {
            dataBase.getPlayerShips(player);
            if (player.getShips().size() == 0) {
                playerManager.respawnPlayer(player, 0, 0);
            }
        } else {
            networkSystem.sendPacketTo(new PacketOpenGui(EnumGui.SelectFaction), player);
        }
    }

    @Override
    protected void input() {

    }

    @Override
    protected void postInputUpdate() {

    }

    @Override
    protected void update(double delta) {
        profiler.startSection("update");
        world.update(delta);
        profiler.endStartSection("network");
        networkSystem.networkTick();
        profiler.endSection();
    }

    @Override
    protected void render(float interpolation) {

    }

    @Override
    protected void setFps(int tps) {
        this.tps = tps;
    }

    @Override
    protected void last() {

    }

    @Override
    protected void clear() {
        LOGGER.log(Level.INFO, "Terminating network...");
        if (getNetworkSystem() != null && !isSinglePlayer) {
            getNetworkSystem().terminateEndpoints();
        }

        LOGGER.log(Level.INFO, "Save base data...");
        dataBase.save();
        LOGGER.log(Level.INFO, "Clearing world...");
        world.clear();
        world = null;
        LOGGER.log(Level.INFO, "Stopped");
    }

    @Override
    protected boolean isVSync() {
        return false;
    }

    public void stop() {
        LOGGER.log(Level.INFO, "Stopping server...");
        isRunning = false;
    }

    public NetworkSystem getNetworkSystem() {
        return networkSystem;
    }

    public ServerStatusResponse getStatus() {
        return new ServerStatusResponse(10, "0.0.4");
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getTps() {
        return tps;
    }

    public SimpleDataBase getDataBase() {
        return dataBase;
    }

    public WorldServer getWorld() {
        return world;
    }

    public static MainServer getServer() {
        return instance;
    }

    public Profiler getProfiler() {
        return profiler;
    }

}
